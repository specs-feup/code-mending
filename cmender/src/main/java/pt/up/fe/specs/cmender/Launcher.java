package pt.up.fe.specs.cmender;

import org.apache.commons.cli.*;

import java.util.List;

public class Launcher {
    private static final String USAGE_STRING = "Usage: cmender [options] file...";

    private static CMenderInvocation parseArgs(String[] args) {
        var options = new Options();

        options.addOption(Option.builder("v")
                .longOpt("verbose")
                .desc("Use verbose output")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Display usage and available options")
                .build());

        var helpFormatter = new HelpFormatter();
        var invocationBuilder = CMenderInvocation.builder();

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                helpFormatter.printHelp(USAGE_STRING, options);
                System.exit(0);
            }

            var files = cmd.getArgs();

            if (files.length == 0) {
                System.out.println("error: no input files specified");
                helpFormatter.printHelp(USAGE_STRING, options);
                System.exit(1);
            }

            invocationBuilder = invocationBuilder
                    .command(args)
                    .verbose(cmd.hasOption("v"))
                    .files(List.of(files));

        } catch (ParseException e) {
            System.out.println("error: command line parsing failed: " + e.getMessage());
            helpFormatter.printHelp(USAGE_STRING, options);
            System.exit(1);
        }

        return invocationBuilder.build();
    }

    public static void main(String[] args) {
        var invocation = parseArgs(args);

        System.out.println(invocation);
    }
}
