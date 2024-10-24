package pt.up.fe.specs.cmender.mending;

import pt.up.fe.specs.cmender.CMenderInvocation;
import pt.up.fe.specs.cmender.cli.CliReporting;
import pt.up.fe.specs.cmender.data.CMenderDataManager;
import pt.up.fe.specs.cmender.data.MendingDirData;
import pt.up.fe.specs.cmender.diag.DiagExporter;
import pt.up.fe.specs.cmender.diag.DiagExporterException;
import pt.up.fe.specs.cmender.diag.DiagExporterInvocation;
import pt.up.fe.specs.cmender.diag.DiagExporterResult;
import pt.up.fe.specs.cmender.diag.DiagExporterSingleSourceResult;
import pt.up.fe.specs.cmender.diag.Diagnostic;
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

    // because "mending" is the process, not the result. "mends" is the result
    private static final String MENDFILE_NAME = "mends";

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

        // TODO support multiple files (also think if one diag-exporter invocation per file is the best approach
        //  or have a single invocation for multiple files)
        // TODO how should multithreading be handled? (e.g., one thread per batch of files etc.)

        var file = files.getFirst();

        System.out.println(file);

        /*var sourceFileCopy = CMenderDataManager.createMendingDir(file, MENDING_DISCLAIMER_IN_SOURCE, MENDFILE_NAME);

        System.out.println(sourceFileCopy);

        if (sourceFileCopy == null) {
            return null;
        }*/

        var mendingDirData = CMenderDataManager.createMendingDir(file, MENDING_DISCLAIMER_IN_SOURCE, MENDFILE_NAME);

        System.out.println(mendingDirData);

        if (mendingDirData == null) {
            return null;
        }

        return CMenderResult.builder()
                .invocation(menderInvocation)
                .sourceResults(List.of(mend(file, mendingDirData)))
                .build();
    }

    private SourceResult mend(String sourceFile, MendingDirData mendingDirData/*String sourceFileCopy*/) {
        String sourceFileCopy = mendingDirData.sourceFileCopyPath();

        var mendingTable = new MendingTable();
        var maxTotalIterations = menderInvocation.getMaxTotalIterations();

        // TODO think about stopping criteria (to avoid infinite loops)
        //  1) max number of total iterations (to avoid infinite loops) -> how do we decide the value? because it depends on the number of
        //      diagnostics. if we select to process one diagnostic at a time, we require a higher number of iterations.
        //  2) max number of successive mending failures (to avoid infinite loops) -> by failure we mean that the mend was not helpful (what do we consider helpful?);
        //      perhaps have methods that hash the state of the MendingTable and compare them between consecutive iterations or look at the variation through time (e.g., derivative)
        //  3) total time -> maybe not the best approach, because the time can vary a lot depending on the number of diagnostics and the complexity of the mends.
        //      but can also be a more conservative approach (and we can do load tests to find a good estimate.

        var timedSourceResult = TimeMeasure.measureElapsed(() -> {
            var success = false;
            var finished = false;
            var currentIteration = 0L;

            var diagExporterTotalTime = 0L;
            var mendingTotalTime = 0L;
            var mendfileWritingTotalTime = 0L;


            List<DiagnosticShortInfo> unknownDiags = new ArrayList<>();

            List<SourceIterationResult> iterationResults = new ArrayList<>();

            while (!finished && currentIteration++ < maxTotalIterations) {
                var sourceIterationResult = mendingIteration(mendingDirData, currentIteration, mendingTable);

                if (sourceIterationResult == null) {
                    // TODO
                    return null;
                }

                diagExporterTotalTime += sourceIterationResult.diagExporterTime();
                mendingTotalTime += sourceIterationResult.mendingTime();
                mendfileWritingTotalTime += sourceIterationResult.mendfileWritingTime();

                success = sourceIterationResult.mendResult().success();
                finished = success || sourceIterationResult.mendResult().finishedPrematurely(menderInvocation);

                unknownDiags.addAll(sourceIterationResult.mendResult().unknownDiags());

                iterationResults.add(sourceIterationResult);
            }

            return SourceResult.builder()
                    .success(success)
                    .iterations(currentIteration - 1)
                    .unknownDiags(new ArrayList<>(unknownDiags))
                    .iterationResults(iterationResults)

                    // Total times in NS
                    .diagExporterTotalTime(diagExporterTotalTime)
                    .mendingTotalTime(mendingTotalTime)
                    .mendfileWritingTotalTime(mendfileWritingTotalTime)

                    // Total times in MS
                    .diagExporterTotalTimeMs(TimeMeasure.milliseconds(diagExporterTotalTime))
                    .mendingTotalTimeMs(TimeMeasure.milliseconds(mendingTotalTime))
                    .mendfileWritingTotalTimeMs(TimeMeasure.milliseconds(mendfileWritingTotalTime))

                    .build();
        });

        var result = timedSourceResult.result();

        if (result == null) {
            // TODO
            return null;
        }

        CliReporting.info("Source file '%s' was %s", sourceFileCopy, result.success() ? "successfully mended" : "unsuccessfully mended");
        Logging.FILE_LOGGER.info("source file '{}' was {}", sourceFileCopy, result.success() ? "successfully mended" : "unsuccessfully mended");

        var totalTime = timedSourceResult.elapsedTime();

        long otherTotalTime = totalTime - result.diagExporterTotalTime() - result.mendingTotalTime() - result.mendfileWritingTotalTime();

        return result.toBuilder()
                .sourceFile(sourceFile)

                // Total times in NS
                .totalTime(totalTime)
                .otherTotalTime(otherTotalTime)

                // Total times in MS
                .totalTimeMs(TimeMeasure.milliseconds(totalTime))
                .otherTotalTimeMs(TimeMeasure.milliseconds(otherTotalTime))

                // Percentage of total times
                .diagExporterTotalTimePercentage(TimeMeasure.percentage(totalTime, result.diagExporterTotalTime()))
                .mendingTotalTimePercentage(TimeMeasure.percentage(totalTime, result.mendingTotalTime()))
                .mendfileWritingTotalTimePercentage(TimeMeasure.percentage(totalTime, result.mendfileWritingTotalTime()))
                .otherTotalTimePercentage(TimeMeasure.percentage(totalTime, otherTotalTime))

                .build();
    }

    private SourceIterationResult mendingIteration(MendingDirData mendingDirData, long currentIteration, MendingTable mendingTable) {
        var timedResult = TimeMeasure.measureElapsed(() -> {
            long mendfileWritingTime = 0;

            TimedResult<DiagExporterResult> diagExporterTimedResult = callDiagExporter(mendingDirData);
            DiagExporterResult diagExporterResult = diagExporterTimedResult.result();

            if (diagExporterResult == null) {
                // TODO
                return null;
            }

            // Because we only process just one file at a time
            var firstSourceResult = diagExporterResult.sourceResults().getFirst();

            TimedResult<DiagnosticMendResult> mendingTimedResult = processDiagExporterSourceResult(firstSourceResult, mendingTable, mendingDirData);
            DiagnosticMendResult diagnosticMendingResult = mendingTimedResult.result();

            // avoid writing the mendfile if no mends were applied (avoid unnecessary file writes)
            // e.g., if we have an unknown diagnostic we don't want to write a mendfile because we skip the mending
            // iteration
            if (diagnosticMendingResult.appliedMend() || !menderInvocation.isCreateMendfileOnlyOnAlterations()) {
                mendfileWritingTime = writeMendfile(mendingTable, mendingDirData, currentIteration);
            }

            var errorDiags = firstSourceResult.diags().stream()
                    .filter(Diagnostic::isError)
                    .toList();

            return SourceIterationResult.builder()
                    .errorCount(firstSourceResult.errorCount())
                    .fatalCount(firstSourceResult.fatalCount())
                    .diags(errorDiags.stream().map(DiagnosticShortInfo::from).toList())
                    .mendResult(diagnosticMendingResult)

                    // Iteration times in NS
                    .diagExporterTime(diagExporterTimedResult.elapsedTime())
                    .mendingTime(mendingTimedResult.elapsedTime())
                    .mendfileWritingTime(mendfileWritingTime)

                    // Iteration times in MS
                    .diagExporterTimeMs(TimeMeasure.milliseconds(diagExporterTimedResult.elapsedTime()))
                    .mendingTimeMs(TimeMeasure.milliseconds(mendingTimedResult.elapsedTime()))
                    .mendfileWritingTimeMs(TimeMeasure.milliseconds(mendfileWritingTime))

                    .build();
        });

        var result = timedResult.result();

        if (result == null) {
            // TODO
            return null;
        }

        var time = timedResult.elapsedTime();
        var otherTime = time - result.diagExporterTime() -
                            result.mendingTime() - result.mendfileWritingTime();

        return result.toBuilder()
                // Iteration times in NS
                .time(time)
                .otherTime(otherTime)

                // Iteration times in MS
                .timeMs(TimeMeasure.milliseconds(time))
                .otherTimeMs(TimeMeasure.milliseconds(otherTime))

                // Percentage of iteration times
                .diagExporterTimePercentage(TimeMeasure.percentage(time, result.diagExporterTime()))
                .mendingTimePercentage(TimeMeasure.percentage(time, result.mendingTime()))
                .mendfileWritingTimePercentage(TimeMeasure.percentage(time, result.mendfileWritingTime()))
                .otherTimePercentage(TimeMeasure.percentage(time, otherTime))
                .build();
    }

    private TimedResult<DiagExporterResult> callDiagExporter(MendingDirData mendingDirData) {
        return TimeMeasure.measureElapsed(() -> {
            try {
                return diagExporter.run(
                        DiagExporterInvocation
                                .builder()
                                .includePaths(List.of(mendingDirData.includePath()))
                                .files(List.of(mendingDirData.sourceFileCopyPath()))
                                .outputFilepath("./cmender_diags_output.json") // TODO change name
                                .build());
            } catch (DiagExporterException e) {
                Logging.FILE_LOGGER.error(e.getMessage(), e);
                CliReporting.error(e.getMessage());
                CliReporting.error("could not export Clang diagnostics from file: '%s'", mendingDirData.sourceFileCopyPath());
                return null;
            }
        });
    }

    private TimedResult<DiagnosticMendResult> processDiagExporterSourceResult(
            DiagExporterSingleSourceResult diagExporterSingleSourceResult, MendingTable mendingTable, MendingDirData mendingDirData) {
        return TimeMeasure.measureElapsed(() -> {
            // TODO we can also have a flag to finish only if there are no diagnostics (e.g., include warnings)

            if (!diagExporterSingleSourceResult.hasErrorsOrFatals()) {
                return DiagnosticMendResult.builder()
                        .success(true)
                        .unknownDiags(List.of())
                        .mendedDiags(List.of())
                        .build();
            }

            // TODO maybe process warnings (?) this might change even more the original code (for the worse) because it might
            // have been present on the original code

            // TODO for now we don't have a need to to process notes, but they might be useful in the future

            var firstError = diagExporterSingleSourceResult.getFirstErrorOrFatal();

            System.out.println(firstError);

            var diagnosticID = DiagnosticID.fromIntID(firstError.id());

            switch (diagnosticID) {
                case DiagnosticID.UNKNOWN -> {
                    MendingHandlers.handleUnknown(firstError, mendingTable);
                    return DiagnosticMendResult.builder()
                            .success(false)
                            .unknownDiags(List.of(DiagnosticShortInfo.from(firstError)))
                            .mendedDiags(List.of())
                            .build();
                }
                case DiagnosticID.EXT_IMPLICIT_FUNCTION_DECL_C99 ->
                        MendingHandlers.handleExtImplicitFunctionDeclC99(firstError, mendingTable);
                case DiagnosticID.ERR_UNDECLARED_VAR_USE ->
                        MendingHandlers.handleErrUndeclaredVarUse(firstError, mendingTable);
                case DiagnosticID.ERR_UNDECLARED_VAR_USE_SUGGEST ->
                        MendingHandlers.handleErrUndeclaredVarUseSuggest(firstError, mendingTable);
                case DiagnosticID.ERR_TYPECHECK_CONVERT_INCOMPATIBLE ->
                        MendingHandlers.handleErrTypecheckConvertIncompatible(firstError, mendingTable);
                case DiagnosticID.ERR_TYPECHECK_INVALID_OPERANDS ->
                        MendingHandlers.handleErrTypecheckInvalidOperands(firstError, mendingTable);
                case DiagnosticID.ERR_PP_FILE_NOT_FOUND ->
                        MendingHandlers.handleErrPPFileNotFound(firstError, mendingTable, mendingDirData);
                case DiagnosticID.ERR_TYPECHECK_DECL_INCOMPLETE_TYPE ->
                        MendingHandlers.handleErrTypecheckDeclIncompleteType(firstError, mendingTable);
                case DiagnosticID.ERR_NO_MEMBER ->
                        MendingHandlers.handleErrNoMember(firstError, mendingTable);
            }

            // TODO improve? this success=false is misleading because it can be successful in the sense that it
            //  processed the last diagnostic but we only know that next iteration after writing the mending file
            //  and running the diag exporter again.
            //  perhaps we just need to rename the field

            return DiagnosticMendResult.builder()
                    .success(false)
                    .appliedMend(true)
                    .unknownDiags(List.of())
                    .mendedDiags(List.of(DiagnosticShortInfo.from(firstError)))
                    .build();
        });
    }

    private long writeMendfile(MendingTable table, MendingDirData mendingDirData, long currentIteration) {
        var sourceFileCopyPath = mendingDirData.sourceFileCopyPath();

        return TimeMeasure.measureElapsed(() -> {
            try {
                var mendingDirPath = Paths.get(sourceFileCopyPath).getParent();

                var mendingFile = Paths.get(mendingDirPath.toString(), MENDFILE_FILENAME).toFile();

                var writer = new BufferedWriter(new FileWriter(mendingFile));

                writer.write(MENDING_DISCLAIMER_IN_HEADER);
                writer.newLine();
                table.writeSymbolDecls(writer);

                if (menderInvocation.isCreateMendfileCopyPerIteration()) {
                    var copyPath = Paths.get(mendingDirPath.toString(), MENDFILE_NAME + "_" + currentIteration + ".h").toFile();

                    Files.copy(mendingFile.toPath(), copyPath.toPath());

                    mendingDirData.mendfileCopyPaths().add(copyPath.getCanonicalPath());
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
