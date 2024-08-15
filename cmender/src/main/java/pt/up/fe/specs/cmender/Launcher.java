package pt.up.fe.specs.cmender;

import pt.up.fe.specs.cmender.cli.CliArgsParser;
import pt.up.fe.specs.cmender.cli.CliArgsParserException;
import pt.up.fe.specs.cmender.cli.CliReporting;
import pt.up.fe.specs.cmender.diag.DiagExporter;
import pt.up.fe.specs.cmender.logging.Logging;

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

        var diagExporter = new DiagExporter(invocation.getDiagExporterPath());
    }
}
