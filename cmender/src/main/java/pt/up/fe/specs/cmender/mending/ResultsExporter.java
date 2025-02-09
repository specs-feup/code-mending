package pt.up.fe.specs.cmender.mending;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import pt.up.fe.specs.cmender.CMenderInvocation;
import pt.up.fe.specs.cmender.cli.CliReporting;
import pt.up.fe.specs.cmender.data.MendingDirData;
import pt.up.fe.specs.cmender.logging.Logging;
import pt.up.fe.specs.cmender.utils.Hashing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

// TODO when exporting we should either raise an error if the exporting directory already exists or delete everything (bad alternative)
//    This should be done because if the user outputs different results to the same output directory he might get stale results from previous iterations
// It's better to export the results after and not directly work on the output directory
//   mainly because errors can happen, and we might not want incomplete or corrupted results to be saved
public class ResultsExporter {

    // Results should be (so far taking account one source file):
    // - [default] the mendfile for the each source file + source file copy
    //           - [optional] mendfile copy of for each iteration
    // - [default]  JSON file with the structured report from the mending process for each source file (e.g., time spent in each step, number of iterations, etc.)
    // - [optional] JSON (or Yaml) file with the structured results from the diag-exporter process for each source file (e.g., the diagnostics for each source file)
    //           - [optional] copy for each iteration

    public static void exportResults(CMenderInvocation invocation, List<MendingDirData> mendingDirDatas, CMenderReport report) {
        // TODO this code is really bad and messy. It should be refactored
        var outputPath = Paths.get(invocation.getOutput());

        ObjectMapper mapper = new ObjectMapper();
        //mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            System.out.println(mendingDirDatas);
            Files.createDirectories(outputPath);

            var reportFilePath = outputPath.resolve(invocation.getReportFilename());

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(reportFilePath.toString()), report);

            for (var mendingDirData : mendingDirDatas) {
                // TODO maybe we could copy the temp dir straight to the output dir. so far there are no options here
                //    that configure the output such that it
                exportMendingDir(outputPath, mendingDirData, invocation);
            }
        } catch (IOException e) {
            // TODO granular error messages and handling of fails
            e.printStackTrace();
            CliReporting.error("Could not save report to " + invocation.getOutput());
            Logging.FILE_LOGGER.error("Could not save report to {}", invocation.getOutput(), e);
        }
    }

    private static void exportMendingDir(Path outputPath, MendingDirData mendingDirData, CMenderInvocation invocation) throws IOException {
        if (mendingDirData == null) {
            System.out.println("mendingDirData is null for " + outputPath);
            return;
        }

        // TODO validate filename. the source file name should not have any special characters
        //    because it will be used as a directory name
        var mendingDirName = Paths.get(mendingDirData.sourceFilePath()).getFileName().toString() +
                                    "_" + Hashing.sha1(mendingDirData.sourceFilePath());
        var mendingOutputPath = Files.createDirectories(Paths.get(outputPath.toString(), mendingDirName));

        // export source file
        Files.copy(
                Paths.get(mendingDirData.sourceFileCopyPath()),
                Paths.get(mendingOutputPath.toString(), Paths.get(mendingDirData.sourceFileCopyPath()).getFileName().toString()),
                StandardCopyOption.REPLACE_EXISTING);

        // export mendfile copies
        if (invocation.isCreateMendfileCopyPerIteration()) { // TODO if probably not needed because we might just want to move the temp directory
            var mendfileCopies = Files.createDirectories(Paths.get(mendingOutputPath.toString(), "mendfileCopies"));
            FileUtils.copyDirectory(new File(mendingDirData.mendfileCopiesDirPath()), new File(mendfileCopies.toString()));
        }

        // export includes
        var includes = Files.createDirectories(Paths.get(mendingOutputPath.toString(), "includes"));
        FileUtils.copyDirectory(new File(mendingDirData.includePath()), new File(includes.toString()));

        // export diag results
        if (invocation.isOutputDiagsOutput()) { // TODO if probably not needed because we might just want to move the temp directory
            var diagResults = Files.createDirectories(Paths.get(mendingOutputPath.toString(), "diagsOutputs"));
            FileUtils.copyDirectory(new File(mendingDirData.diagsDirPath()), new File(diagResults.toString()));
        }

        // export source report (if it exists)
        if (invocation.isReportPerSource()) { // TODO if probably not needed because we might just want to move the temp directory
            Files.copy(
                    Paths.get(mendingDirData.sourceReportPath()),
                    Paths.get(mendingOutputPath.toString(), Paths.get(mendingDirData.sourceReportPath()).getFileName().toString()),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
