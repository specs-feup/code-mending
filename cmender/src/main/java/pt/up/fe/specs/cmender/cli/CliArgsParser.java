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
    private static final String HELP_SHORT = "h";
    private static final String HELP_LONG = "help";

    private static final String VERSION_LONG = "version";

    private static final String DIAG_EXPORTER_SHORT = "dex";
    private static final String DIAG_EXPORTER_LONG = "diag-exporter";

    private static final String MENDFILE_COPY_PER_ITERATION_SHORT = "mfci";
    private static final String MENDFILE_COPY_PER_ITERATION_LONG = "mendfile-copy-per-iteration";

    private static final String VERBOSE_SHORT = "v";
    private static final String VERBOSE_LONG = "verbose";

    public static final Options OPTIONS = new Options()
            .addOption(Option.builder(HELP_SHORT)
                .longOpt(HELP_LONG)
                .desc("Display usage and available options")
                .build())
            .addOption(Option.builder()
                .longOpt(VERSION_LONG)
                .desc("Print version information")
                .build())
            .addOption(Option.builder(DIAG_EXPORTER_SHORT)
                .longOpt(DIAG_EXPORTER_LONG)
                .desc("Path for diag-exporter executable")
                .argName("path")
                .hasArg()
                .optionalArg(true)
                .type(String.class)
                .build())
            .addOption(Option.builder(MENDFILE_COPY_PER_ITERATION_SHORT)
                .longOpt(MENDFILE_COPY_PER_ITERATION_LONG)
                .desc("Create a mendfile copy per iteration")
                .build())
            .addOption(Option.builder(VERBOSE_SHORT)
                .longOpt(VERBOSE_LONG)
                .desc("Use verbose output")
                .build());
            /*.addOption(Option.builder("dex")
                .longOpt("diag-exporter")
                .desc("Path for diag-exporter executable")
                .hasArg()
                .numberOfArgs(1)
                .argName("path")
                .type(String.class)
                .valueSeparator()
                .build());*/

    public static CMenderInvocation parseArgs(String[] args) throws CliArgsParserException {
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(OPTIONS, args);

            var files = cmd.getArgs();

            if (!cmd.hasOption(HELP_SHORT) && !cmd.hasOption(VERSION_LONG)) {
                if (!cmd.hasOption(DIAG_EXPORTER_SHORT)) {
                    throw CliArgsParserException.ofMissingOption(DIAG_EXPORTER_SHORT);
                }

                if (cmd.getOptionValue(DIAG_EXPORTER_SHORT) == null) {
                    // TODO improve by using either short or long version (depending on what was used)
                    throw CliArgsParserException.ofOptionMissingArgument(DIAG_EXPORTER_SHORT);
                }

                if (files.length == 0) {
                    throw CliArgsParserException.ofMissingInputFiles();
                }
            }

            return CMenderInvocation.builder()
                    .invocation(List.of(args))
                    .version(cmd.hasOption(VERSION_LONG))
                    .help(cmd.hasOption(HELP_SHORT))
                    .verbose(cmd.hasOption(VERBOSE_SHORT))
                    .diagExporterPath(cmd.getOptionValue(DIAG_EXPORTER_SHORT))
                    .createMendfileCopyPerIteration(cmd.hasOption(MENDFILE_COPY_PER_ITERATION_SHORT))
                    .files(List.of(files))
                    .build();
        } catch (ParseException e) {
            throw new CliArgsParserException(e);
        }
    }
}
