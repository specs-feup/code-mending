package pt.up.fe.specs.cmender.data;

import pt.up.fe.specs.cmender.cli.CliReporting;
import pt.up.fe.specs.cmender.logging.Logging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CMenderDataManager {

    public static final String DATA_DIRPATH;

    public static final String BASE_MENDING_DIRPATH;

    public static final Map<String, String> MENDING_DIRNAMES;

    static {
        DATA_DIRPATH = getCMenderDataDirPath();
        BASE_MENDING_DIRPATH = Paths.get(DATA_DIRPATH, "mending").toString();
        MENDING_DIRNAMES = new HashMap<>();

        try {
            Files.createDirectories(Paths.get(BASE_MENDING_DIRPATH));

        } catch (IOException e) {
            CliReporting.error("failed to create data directory: %s", e.getMessage());
            Logging.FILE_LOGGER.fatal("failed to create data directory: {}", e.getMessage());
        }
    }

    public static String getCMenderDataDirPath() {

        var os = System.getProperty("os.name").toLowerCase();
        var home = System.getProperty("user.home", "./");

        String defaultDataPath;

        // TODO is it worth to add Apache Commons Lang to simplify this?
        if (os.contains("win")) {
            defaultDataPath = Paths.get(home, "AppData", "Local", "CMender", "data").toString();
        } else if (os.contains("mac")) {
            defaultDataPath = Paths.get(home, "Library", "Application Support", "CMender").toString();
        } else {
            defaultDataPath = Paths.get(home, ".CMender", "data").toString();
        }

        return System.getProperty("data.dir", defaultDataPath);
    }

    public static MendingDirData createMendingDir(String sourceFilePathStr, String mendingDisclaimerInSource, String mendfileName) {
        UUID id = UUID.randomUUID();
        Path sourceFilePath = Paths.get(sourceFilePathStr);
        Path mendingDirPath = Paths.get(BASE_MENDING_DIRPATH, id.toString());
        Path includeDirPath = Paths.get(BASE_MENDING_DIRPATH, id.toString(), "includes");

        try {
            // TODO think of garbage collection of old mending directories (maybe temporary directories?)
            //  also maybe customisation to this behaviour (e.g., keep the last N directories)
            Files.createDirectories(mendingDirPath);
            Files.createDirectories(includeDirPath);

            MENDING_DIRNAMES.put(sourceFilePathStr, mendingDirPath.getFileName().toString());

            Path sourceFileCopyPath = Paths.get(mendingDirPath.resolve(sourceFilePath.getFileName()).toFile().getCanonicalPath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFileCopyPath.toFile()));

            // We dont copy the file because we need to add the disclaimer and the include directive
            BufferedReader reader = new BufferedReader(new FileReader(sourceFilePath.toFile()));
                writer.write(mendingDisclaimerInSource);
                writer.newLine();
                writer.write("#include \"./" + mendfileName + ".h\"");
                writer.newLine();
                writer.newLine();

            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                writer.write(line);
                writer.newLine();
            }
            writer.close();
            reader.close();

            // Create the (empty) header file before any diag-exporter calls (to avoid missing header file error)
            var headerFilePath = mendingDirPath.resolve(mendfileName + ".h");
            writer = new BufferedWriter(new FileWriter(headerFilePath.toFile()));
            writer.flush();
            writer.close();

            //return mendingDirPath.toString();
            return MendingDirData.builder()
                    .id(id)
                    .sourceFilePath(sourceFilePathStr)
                    .sourceFileCopyPath(sourceFileCopyPath.toFile().getCanonicalPath())
                    .mendfilePath(headerFilePath.toString())
                    .mendfileCopyPaths(new ArrayList<>())
                    .includePath(includeDirPath.toString())
                    .build();
            //return sourceFileCopyPath.toFile().getCanonicalPath();
        } catch (IOException e) {
            // TODO this exception in the future should be handled by the caller because we will be handling
            //  multiple files at once and we should not stop the process if one file fails to be copied.
            //  We just save the error message and continue with the other files.
            CliReporting.error("failed to create mending directory: '%s'", e.getMessage());
            Logging.FILE_LOGGER.error("failed to create mending directory: '{}'", e.getMessage());
            return null;
        }
    }
}
