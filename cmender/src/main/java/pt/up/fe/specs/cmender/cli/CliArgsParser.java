package pt.up.fe.specs.cmender.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import pt.up.fe.specs.cmender.CMenderInvocation;

import java.util.List;

public class CliArgsParser {
    public static final Options OPTIONS = new Options()
            .addOption(Option.builder("v")
                    .longOpt("verbose")
                    .desc("Use verbose output")
                    .build())
            .addOption(Option.builder("h")
                .longOpt("help")
                .desc("Display usage and available options")
                .build())
            .addOption(Option.builder()
                .longOpt("version")
                .desc("Print version information")
                .build())
            .addOption(Option.builder("dex")
                .longOpt("diag-exporter")
                .desc("Path for diag-exporter executable")
                .hasArg()
                .numberOfArgs(1)
                .argName("path")
                .type(String.class)
                .valueSeparator()
                .build());

    public static CMenderInvocation parseArgs(String[] args) throws CliArgsParserException {
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(OPTIONS, args);

            var files = cmd.getArgs();

            return CMenderInvocation.builder()
                    .command(List.of(args))
                    .version(cmd.hasOption("version"))
                    .help(cmd.hasOption("h"))
                    .verbose(cmd.hasOption("v"))
                    .diagExporterPath(cmd.getOptionValue("dex"))
                    .files(List.of(files))
                    .build();
        } catch (ParseException e) {
            throw new CliArgsParserException(e);
        }
    }
}
