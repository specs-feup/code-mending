package pt.up.fe.specs.cmender;

import pt.up.fe.specs.cmender.cli.CliArgsParser;
import pt.up.fe.specs.cmender.cli.CliArgsParserException;
import pt.up.fe.specs.cmender.cli.CliReporting;
import pt.up.fe.specs.cmender.logging.Logging;
import pt.up.fe.specs.cmender.mending.MendingEngine;

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

        double diagExporterTotal = 0;
        double total = 0;
        int n = 1;

        for (int i = 0; i < n; i++) {
            var result = new MendingEngine(invocation).execute();

            //System.out.println(result);
            //diagExporterTotal += result.diagExporterTotalTimeMs();
            //total += result.totalTimeMs();
        }

        System.out.println("avg diag exporter total time: " + diagExporterTotal/n);
        System.out.println("avg total time: " + total/n);
    }
}
