package pt.up.fe.specs.cmender;

import org.apache.commons.cli.*;

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

        var helpFormatter = new HelpFormatter();

        var invocationBuilder = CMenderInvocation.builder();

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                System.out.printf("overview: %s - %s by %s\n", properties.name(), properties.description(), properties.vendor());
                helpFormatter.printHelp(USAGE_STRING, options);
                System.exit(0);
            }

            if (cmd.hasOption("version")) {
                System.out.println(properties.name() + " by " + properties.vendor());
                System.out.printf("Version: %s\n", properties.version());
                System.out.printf("Release Date: %s\n", properties.date());
                System.exit(0);
            }

            var files = cmd.getArgs();

            if (files.length == 0) {
                System.err.println("error: no input files specified");
                helpFormatter.printHelp(USAGE_STRING, options);
                System.exit(1);
            }

            invocationBuilder = invocationBuilder
                    .command(List.of(args))
                    .verbose(cmd.hasOption("v"))
                    .files(List.of(files));

        } catch (ParseException e) {
            System.err.println("error: command line parsing failed: " + e.getMessage());
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

        System.out.println(properties);

        var invocation = parseCliArgs(args, properties);

        System.out.println(invocation);
    }
}
