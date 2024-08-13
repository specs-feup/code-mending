package pt.up.fe.specs.cmender;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import pt.up.fe.specs.cmender.cli.CliReporting;
import pt.up.fe.specs.cmender.diag.DiagExporter;
import pt.up.fe.specs.cmender.logging.Logging;
import pt.up.fe.specs.cmender.diag.DiagExporter;

import java.util.List;

public class Launcher {
    private static final String USAGE_STRING = "jar cmender.jar [options] file...";

    private static CMenderInvocation parseCliArgs(String[] args, CMenderProperties properties) {
        var options = new Options();

        options.addOption(Option.builder("v")
                .longOpt("verbose")
                .desc("Use verbose output")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Display usage and available options")
                .build());

        options.addOption(Option.builder()
                .longOpt("version")
                .desc("Print version information")
                .build());

        options.addOption(Option.builder("dex")
                .longOpt("diag-exporter")
                .desc("Path for diag-exporter executable")
                .required()
                .hasArg()
                .build());

        var helpFormatter = new HelpFormatter();

        var invocationBuilder = CMenderInvocation.builder();

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                CliReporting.info("overview: %s - %s by %s\n", properties.name(), properties.description(), properties.vendor());
                helpFormatter.printHelp(USAGE_STRING, options);
                System.exit(0);
            }

            if (cmd.hasOption("version")) {
                CliReporting.info(properties.name() + " by " + properties.vendor());
                CliReporting.info("Version: %s", properties.version());
                CliReporting.info("Release Date: %s", properties.date());
                System.exit(0);
            }

            var files = cmd.getArgs();

            if (files.length == 0) {
                CliReporting.error("no input files specified");
                helpFormatter.printHelp(USAGE_STRING, options);
                System.exit(1);
            }

            invocationBuilder = invocationBuilder
                    .command(List.of(args))
                    .verbose(cmd.hasOption("v"))
                    .diagExporterPath(cmd.getOptionValue("dex"))
                    .files(List.of(files));

        } catch (ParseException e) {
            System.err.println("error: command line parsing failed. " + e.getMessage());
            helpFormatter.printHelp(USAGE_STRING, options);
            System.exit(1);
        }

        return invocationBuilder.build();
    }

    public static void main(String[] args) {
        var properties = CMenderProperties.get();

        if (properties == null) {
            System.exit(1);
        }

        Logging.FILE_LOGGER.debug(properties);

        var invocation = parseCliArgs(args, properties);

        Logging.FILE_LOGGER.debug(invocation);

        var diagExporter = new DiagExporter(invocation.getDiagExporterPath());
    }
}
