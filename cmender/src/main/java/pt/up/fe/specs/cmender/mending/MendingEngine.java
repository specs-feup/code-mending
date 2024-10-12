package pt.up.fe.specs.cmender.mending;

import pt.up.fe.specs.cmender.CMenderInvocation;
import pt.up.fe.specs.cmender.cli.CliReporting;
import pt.up.fe.specs.cmender.data.CMenderDataManager;
import pt.up.fe.specs.cmender.diag.DiagExporter;
import pt.up.fe.specs.cmender.diag.DiagExporterException;
import pt.up.fe.specs.cmender.diag.DiagExporterInvocation;
import pt.up.fe.specs.cmender.diag.DiagExporterResult;
import pt.up.fe.specs.cmender.diag.DiagExporterSingleSourceResult;
import pt.up.fe.specs.cmender.diag.DiagnosticID;
import pt.up.fe.specs.cmender.logging.Logging;
import pt.up.fe.specs.cmender.utils.TimeMeasure;
import pt.up.fe.specs.cmender.utils.TimedResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MendingEngine {
    private static final String MENDING_DISCLAIMER_IN_SOURCE = """
            /*
             * This file was automatically mended by the CMender tool.
             * A header file with the necessary mends (symbol declarations) is included.
             * The mending process is not guaranteed to be correct, nor to preserve the original semantics.
             * The user should review the changes and test the code if needed.
             */""";

    private static final String MENDING_DISCLAIMER_IN_HEADER = """
            /*
             * This header file  was automatically generated by the CMender tool.
             * It contains the necessary mends to fix the errors found by the DiagExporter tool.
             */""";

    // TODO change to "mends" or "mend"? because mending is the process, not the result
    private static final String MENDFILE_NAME = "mending";

    private static final String MENDFILE_FILENAME = MENDFILE_NAME + ".h";

    private final DiagExporter diagExporter;

    private final CMenderInvocation menderInvocation;

    public MendingEngine(CMenderInvocation menderInvocation) {
        diagExporter = new DiagExporter(menderInvocation.getDiagExporterPath());
        this.menderInvocation = menderInvocation;
    }

    public CMenderResult execute() {
        var files = getExistingValidFiles(getAbsolutePaths(menderInvocation.getFiles()));

        if (files.isEmpty()) {
            CliReporting.warning("no valid input files provided, exiting");
            Logging.FILE_LOGGER.warn("no valid input files provided, exiting");
            return null;
        }

        CliReporting.warning("only one file is supported at the moment, the first file will be used");
        Logging.FILE_LOGGER.warn("only one file is supported at the moment, the first file will be used");

        // TODO support multiple files (also think if one invocation per file is the best approach
        //  or have a single invocation for multiple files)
        // TODO how should multithreading be handled? (e.g., one thread per batch of files etc.)

        var file = files.getFirst();

        System.out.println(file);

        var sourceFileCopy = CMenderDataManager.createMendingDir(file, MENDING_DISCLAIMER_IN_SOURCE, MENDFILE_NAME);

        System.out.println(sourceFileCopy);

        if (sourceFileCopy == null) {
            return null;
        }

        return mend(sourceFileCopy);
    }

    private CMenderResult mend(String sourceFileCopy) {
        var mendingTable = new MendingTable();
        var maxTotalIterations = menderInvocation.getMaxTotalIterations();

        // TODO think about stopping criteria (to avoid infinite loops)
        //  1) max number of total iterations (to avoid infinite loops) -> how do we decide the value? because it depends on the number of
        //      diagnostics. if we select to process one diagnostic at a time, we require a higher number of iterations.
        //  2) max number of successive mending failures (to avoid infinite loops) -> by failure we mean that the mend was not helpful (what do we consider helpful?);
        //      perhaps have methods that hash the state of the MendingTable and compare them between consecutive iterations or look at the variation through time (e.g., derivative)
        //  3) total time -> maybe not the best approach, because the time can vary a lot depending on the number of diagnostics and the complexity of the mends.
        //      but can also be a more conservative approach (and we can do load tests to find a good estimate.

        var cMenderTimedResult = TimeMeasure.measureElapsed(() -> {
            var success = false;
            var finished = false;
            var currentIteration = 0;

            var diagExporterTotalTime = 0L;
            var mendingTotalTime = 0L;
            var mendfileWritingTotalTime = 0L;

            String unknownDiag = null;

            while (!finished && currentIteration++ < maxTotalIterations) {
                TimedResult<DiagExporterResult> diagExporterTimedResult = callDiagExporter(sourceFileCopy);

                diagExporterTotalTime += diagExporterTimedResult.elapsedTime();

                DiagExporterResult diagExporterResult = diagExporterTimedResult.result();

                if (diagExporterResult == null) {
                    return null;
                }

                // Because we only process just one file at a time
                var firstSourceResult = diagExporterResult.sourceResults().getFirst();

                TimedResult<DiagnosticMendResult> mendingTimedResult = processSourceResult(firstSourceResult, mendingTable);

                unknownDiag = mendingTimedResult.result().unknownDiag();

                mendingTotalTime += mendingTimedResult.elapsedTime();

                DiagnosticMendResult diagnosticMendingResult = mendingTimedResult.result();

                if (diagnosticMendingResult.appliedMend()) {
                    long mendfileWritingTime = writeMendingFile(mendingTable, sourceFileCopy, currentIteration);

                    mendfileWritingTotalTime += mendfileWritingTime;
                }

                success = diagnosticMendingResult.success();
                finished = success || diagnosticMendingResult.finishedPrematurely();

                /*if (diagnosticMendingResult.success()) {
                    success = true;
                    finished = true;
                } else {
                    long mendfileWritingTime = writeMendingFile(mendingTable, sourceFileCopy, currentIteration);

                    mendfileWritingTotalTime += mendfileWritingTime;

                    // TODO if this is true should we even write the mendfile? e.g., on unknown diagnostic we dont
                    //    change the MendingTable so we will always get the same result and we will write a repeated mendfile
                    if (diagnosticMendingResult.finishedPrematurely()) {
                        finished = true;
                    }
                }*/
            }

            return CMenderResult.builder()
                    .success(success)

                    // Total time in NS
                    .diagExporterTotalTime(diagExporterTotalTime)
                    .mendingTotalTime(mendingTotalTime)
                    .mendfileWritingTotalTime(mendfileWritingTotalTime)

                    // Total time in MS
                    .diagExporterTotalTimeMs(TimeMeasure.milliseconds(diagExporterTotalTime))
                    .mendingTotalTimeMs(TimeMeasure.milliseconds(mendingTotalTime))
                    .mendfileWritingTotalTimeMs(TimeMeasure.milliseconds(mendfileWritingTotalTime))

                    .iterations(currentIteration - 1)
                    .unknownDiag(unknownDiag) // TODO change when we support tolerance of more than one unknown diag/multiple files

                    .build();
        });

        var result = cMenderTimedResult.result();

        System.out.println(result.success() ? "Code was successfully mended." : "Code was not successfully mended.");

        var totalTime = cMenderTimedResult.elapsedTime();

        long otherTotalTime = totalTime - result.diagExporterTotalTime() - result.mendingTotalTime() - result.mendfileWritingTotalTime();

        return result.toBuilder()
                // Total time in NS
                .totalTime(totalTime)
                .otherTotalTime(otherTotalTime)

                // Total time in MS
                .totalTimeMs(TimeMeasure.milliseconds(totalTime))
                .otherTotalTimeMs(TimeMeasure.milliseconds(otherTotalTime))

                // Percentage of total time
                .diagExporterTotalTimePercentage(TimeMeasure.percentage(totalTime, result.diagExporterTotalTime()))
                .mendingTotalTimePercentage(TimeMeasure.percentage(totalTime, result.mendingTotalTime()))
                .mendfileWritingTotalTimePercentage(TimeMeasure.percentage(totalTime, result.mendfileWritingTotalTime()))
                .otherTotalTimePercentage(TimeMeasure.percentage(totalTime, otherTotalTime))

                .build();
    }

    private TimedResult<DiagExporterResult> callDiagExporter(String sourceFileCopy) {
        return TimeMeasure.measureElapsed(() -> {
            try {
                return diagExporter.run(
                        DiagExporterInvocation
                                .builder()
                                .files(List.of(sourceFileCopy))
                                .outputFilepath("./cmender_diags_output.json") // TODO change name
                                .build());
            } catch (DiagExporterException e) {
                Logging.FILE_LOGGER.error(e.getMessage(), e);
                CliReporting.error(e.getMessage());
                CliReporting.error("could not export Clang diagnostics from file: '%s'", sourceFileCopy);
                return null;
            }
        });
    }

    private TimedResult<DiagnosticMendResult> processSourceResult(DiagExporterSingleSourceResult sourceResult, MendingTable mendingTable) {
        return TimeMeasure.measureElapsed(() -> {
            // TODO we can also have a flag to finish only if there are no diagnostics (e.g., include warnings)

            if (!sourceResult.hasErrors()) {
                return DiagnosticMendResult.builder()
                        .success(true)
                        .unknownDiag(null)
                        .build();
            }

            // TODO process warnings and notes

            var firstError = sourceResult.getFirstError();

            // TODO maybe add a flag to stop/continue on first unknown diagnostic (this allows to possibly reach a better state, or stop if the code is too broken)

            switch (DiagnosticID.fromIntID(firstError.id())) {
                case DiagnosticID.EXT_IMPLICIT_FUNCTION_DECL_C99 ->
                        MendingHandlers.handleExtImplicitFunctionDeclC99(firstError, mendingTable);
                case DiagnosticID.ERR_UNDECLARED_VAR_USE ->
                        MendingHandlers.handleErrUndeclaredVarUse(firstError, mendingTable);
                case DiagnosticID.UNKNOWN -> {
                    MendingHandlers.handleUnknown(firstError, mendingTable);
                    return DiagnosticMendResult.builder()
                            .success(false)
                            .unknownDiag(firstError.description())
                            .build();
                }
            }

            // TODO improve? this success=false is misleading because it can be successful in the sense that it
            //  processed the last diagnostic but we only know that next iteration after writing the mending file
            //  and running the diag exporter again.
            //  perhaps we just need to rename the field

            return DiagnosticMendResult.builder()
                    .success(false)
                    .appliedMend(true)
                    .unknownDiag(null)
                    .build();
        });
    }

    private long writeMendingFile(MendingTable table, String sourceFileCopy, long currentIteration) {
        return TimeMeasure.measureElapsed(() -> {
            try {
                var mendingDirPath = Paths.get(sourceFileCopy).getParent();

                var mendingFile = Paths.get(mendingDirPath.toString(), MENDFILE_FILENAME).toFile();

                var writer = new BufferedWriter(new FileWriter(mendingFile));

                writer.write(MENDING_DISCLAIMER_IN_HEADER);
                writer.newLine();
                table.writeSymbolDecls(writer);

                if (menderInvocation.isCreateMendfileCopyPerIteration()) {
                    var copyPath = Paths.get(mendingDirPath.toString(), MENDFILE_NAME + "_" + currentIteration + ".h").toFile();

                    Files.copy(mendingFile.toPath(), copyPath.toPath());
                }
            } catch (IOException e) {
                Logging.FILE_LOGGER.error(e.getMessage(), e);
                CliReporting.error("could not write mendfile: '%s'", MENDFILE_FILENAME);
            }
        });
    }

    private List<String> getAbsolutePaths(List<String> files) {
        var absolutePaths = new HashSet<String>();

        for (var file : files) {
            try {
                absolutePaths.add(Paths.get(file).toAbsolutePath().toString());
            } catch (InvalidPathException e) {
                CliReporting.error("invalid input file path: '%s'", file);
                Logging.FILE_LOGGER.error("invalid input file path: '{}'", file);
            }
        }

        return new ArrayList<>(absolutePaths);
    }

    private List<String> getExistingValidFiles(List<String> paths) {
        var existingValidFiles = new ArrayList<String>();

        for (var path : paths) {
            var file = new File(path);

            if (!file.exists()) {
                CliReporting.error("file does not exist: '%s'", file);
                Logging.FILE_LOGGER.error("file does not exist: '{}'", file);
            } else if (!file.isFile()) {
                CliReporting.error("not a file: '%s'", file);
                Logging.FILE_LOGGER.error("not a file: '{}'", file);
            } else if (!file.canRead()) {
                CliReporting.error("cannot read file: '%s'", file);
                Logging.FILE_LOGGER.error("cannot read file: '{}'", file);
            } else {
                try {
                    existingValidFiles.add(file.getCanonicalPath());
                } catch (IOException e) {
                    CliReporting.error("failed to get canonical path for file: '%s'", file);
                    Logging.FILE_LOGGER.error("failed to get canonical path for file: '{}'", file);
                }
            }
        }

        return existingValidFiles;
    }
}