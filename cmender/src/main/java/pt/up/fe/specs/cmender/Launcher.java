package pt.up.fe.specs.cmender;

import org.apache.commons.io.FileUtils;
import pt.up.fe.specs.cmender.cli.CliArgsParser;
import pt.up.fe.specs.cmender.cli.CliArgsParserException;
import pt.up.fe.specs.cmender.cli.CliReporting;
import pt.up.fe.specs.cmender.logging.Logging;
import pt.up.fe.specs.cmender.mending.MendingEngine;
import pt.up.fe.specs.cmender.mending.ResultsExporter;

import java.io.File;
import java.io.IOException;

// TODO replace Path.get() for concatenation of subpaths with resolve() as the latter seems robust and is able to normalize the path as well
public class Launcher {
    public static void main(String[] args) {
        var properties = CMenderProperties.get();

        if (properties == null) {
            System.exit(1);
        }

        Logging.FILE_LOGGER.debug(properties);

        CMenderInvocation invocation = null;

        try {
            invocation = CliArgsParser.parseArgs(args);

            Logging.FILE_LOGGER.debug(invocation);

            if (invocation.isHelp()) {
                CliReporting.help(properties, CliArgsParser.OPTIONS);
                System.exit(0);
            }

            if (invocation.isVersion()) {
                CliReporting.version(properties);
                System.exit(0);
            }
        } catch (CliArgsParserException e) {
            CliReporting.error(e.getMessage());
            CliReporting.usage(properties, CliArgsParser.OPTIONS);
            Logging.FILE_LOGGER.error(e);
            System.exit(1);
        }


        var bundle = new MendingEngine(invocation).execute();

        var report = bundle.cmenderReport();
        var mendingDirDatas = bundle.mendingDirDatas();

        ResultsExporter.exportResults(invocation, mendingDirDatas, report);

        // delete the mending directory
        for (var mendingDirData : mendingDirDatas) {
            if (mendingDirData == null) {
                continue;
            }
            try {
                FileUtils.deleteDirectory(new File(mendingDirData.dirPath()));
            } catch (IOException e) {
                CliReporting.error("Could not delete mending directory " + mendingDirData.dirPath());
                Logging.FILE_LOGGER.error("Could not delete mending directory {}", mendingDirData.dirPath(), e);
            }
        }
    }
}
