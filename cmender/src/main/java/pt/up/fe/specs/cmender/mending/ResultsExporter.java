package pt.up.fe.specs.cmender.mending;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;

import pt.up.fe.specs.cmender.CMenderInvocation;
import pt.up.fe.specs.cmender.cli.CliReporting;
import pt.up.fe.specs.cmender.data.MendingDirData;
import pt.up.fe.specs.cmender.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

// It's better to export the results after and not directly work on the output directory
//   mainly because errors can happen, and we might not want incomplete or corrupted results to be saved
public class ResultsExporter {

    // Results should be (so far taking account one source file):
    // - [default] the mendfile for the each source file + source file copy
    //           - [optional] mendfile copy of for each iteration
    // - [default]  JSON file with the structured results from the mending process for each source file (e.g., time spent in each step, number of iterations, etc.)
    // - [optional] JSON (or Yaml) file with the structured results from the diag-exporter process for each source file (e.g., the diagnostics for each source file)
    //           - [optional] copy for each iteration

    public static void exportResults(CMenderInvocation invocation, MendingDirData mendingDirData, CMenderResult result) {
        // TODO this code is really bad and messy. It should be refactored
        var outputPath = Paths.get(invocation.getOutput());

        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            System.out.println(mendingDirData);
            Files.createDirectories(outputPath);

            // TODO should we assume that the user will input the filename without the extension? or allow him to input the extension?
            var resultFilename = invocation.getResultFilename().endsWith(".json") ? invocation.getResultFilename() : invocation.getResultFilename() + ".json";
            var resultFilePath = outputPath.resolve(resultFilename);

            mapper.writeValue(new File(resultFilePath.toString()), result);

            Files.copy(
                    Paths.get(mendingDirData.sourceFileCopyPath()),
                    Paths.get(outputPath.toString(), Paths.get(mendingDirData.sourceFileCopyPath()).getFileName().toString()),
                    StandardCopyOption.REPLACE_EXISTING);

            Files.copy(
                    Paths.get(mendingDirData.mendfilePath()),
                    Paths.get(outputPath.toString(), Paths.get(mendingDirData.mendfilePath()).getFileName().toString()),
                    StandardCopyOption.REPLACE_EXISTING);

            Files.createDirectories(Paths.get(outputPath.toString(), "mendfileCopies"));

            for (var mendfileCopy : mendingDirData.mendfileCopyPaths()) {
                Files.copy(
                        Paths.get(mendfileCopy),
                        Paths.get(outputPath.toString(), "mendfileCopies", Paths.get(mendfileCopy).getFileName().toString()),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            // export includes
            var includes = Files.createDirectories(Paths.get(outputPath.toString(), "includes"));
            FileUtils.copyDirectory(new File(mendingDirData.includePath()), new File(includes.toString()));
            //copyDir(Paths.get(mendingDirData.includePath()), includes);

            // export diag results

            if (invocation.isOutputDiagsOutput()) {
                var diagResults = Files.createDirectories(Paths.get(outputPath.toString(), "diagOutputs"));
                FileUtils.copyDirectory(new File(mendingDirData.diagsDirPath()), new File(diagResults.toString()));
                //copyDir(Paths.get(mendingDirData.diagsDirPath()), diagResults);
            }
        } catch (IOException e) {
            e.printStackTrace();
            CliReporting.error("Could not save result to " + invocation.getOutput());
            Logging.FILE_LOGGER.error("Could not save result to {}", invocation.getOutput(), e);
        }
    }

    private static void copyFile(Path source, Path dest) {
        try {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            CliReporting.error("Could not copy file " + source + " to " + dest);
            Logging.FILE_LOGGER.error("Could not copy file {} to {}", source, dest, e);
        }
    }

    private static void copyDir(Path source, Path dest) {
        try {
            Files.walk(source)
                    .forEach(sourcePath -> {
                        Path destPath = dest.resolve(source.relativize(sourcePath));
                        try {
                            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                            CliReporting.error("Could not copy file " + sourcePath + " to " + destPath);
                            Logging.FILE_LOGGER.error("Could not copy file {} to {}", sourcePath, destPath, e);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            CliReporting.error("Could not copy directory " + source + " to " + dest);
            Logging.FILE_LOGGER.error("Could not copy directory {} to {}", source, dest, e);
        }
    }

}
