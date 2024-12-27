package pt.up.fe.specs.cmender.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import pt.up.fe.specs.cmender.CMenderInvocation;
import pt.up.fe.specs.cmender.logging.Logging;

import java.util.List;

public class CliArgsParser {
    private static final String HELP_SHORT = "h";
    private static final String HELP_LONG = "help";

    private static final String VERSION_LONG = "version";

    private static final String DIAG_EXPORTER_SHORT = "dex";
    private static final String DIAG_EXPORTER_LONG = "diag-exporter";

    private static final String MENDFILE_COPY_PER_ITERATION_SHORT = "mendfile-cpi";
    private static final String MENDFILE_COPY_PER_ITERATION_LONG = "mendfile-copy-per-iteration";

    private static final String MENDFILE_ONLY_ON_ALTERATIONS_SHORT = "mendfile-ooa";
    private static final String MENDFILE_ONLY_ON_ALTERATIONS_LONG = "mendfile-only-on-alterations";

    private static final String DIAGS_OUTPUT_COPY_PER_ITERATION_SHORT = "diags-cpi";
    private static final String DIAGS_OUTPUT_COPY_PER_ITERATION_LONG = "diags-copy-per-iteration";

    private static final String CONTINUE_ON_UNKNOWN_DIAGNOSTIC_SHORT = "cont-on-unknown-diag";
    private static final String CONTINUE_ON_UNKNOWN_DIAGNOSTIC_LONG = "continue-on-unknown-diag";

    private static final String REPORT_PER_SOURCE_SHORT = "rps";
    private static final String REPORT_PER_SOURCE_LONG = "report-per-source";

    private static final String OUTPUT_SHORT = "o";
    private static final String OUTPUT_LONG = "output";

    private static final String OUTPUT_DIAGS_SHORT = "od";
    private static final String OUTPUT_DIAGS_LONG = "output-diags";

    private static final String MENDFILE_FILENAME_SHORT = "mf";
    private static final String MENDFILE_FILENAME_LONG = "mendfile-filename";

    private static final String DIAGS_OUTPUT_FILENAME_SHORT = "dof";
    private static final String DIAGS_OUTPUT_FILENAME_LONG = "diags-output-filename";

    private static final String REPORT_FILENAME_SHORT = "rf";
    private static final String REPORT_FILENAME_LONG = "report-filename";

    private static final String SOURCE_REPORT_FILENAME_SHORT = "srf";
    private static final String SOURCE_REPORT_FILENAME_LONG = "source-report-filename";

    private static final String VERBOSE_SHORT = "v";
    private static final String VERBOSE_LONG = "verbose";

    private static final String THREADS_SHORT = "t";
    private static final String THREADS_LONG = "threads";

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
            .addOption(Option.builder(DIAGS_OUTPUT_COPY_PER_ITERATION_SHORT)
                .longOpt(DIAGS_OUTPUT_COPY_PER_ITERATION_LONG)
                .desc("Create a diagnostics output copy per iteration")
                .build())
            .addOption(Option.builder(MENDFILE_ONLY_ON_ALTERATIONS_SHORT)
                .longOpt(MENDFILE_ONLY_ON_ALTERATIONS_LONG)
                .desc("Create a mendfile only on when an alteration occurs (i.e., new mend is applied)")
                .build())
            .addOption(Option.builder(CONTINUE_ON_UNKNOWN_DIAGNOSTIC_SHORT)
                .longOpt(CONTINUE_ON_UNKNOWN_DIAGNOSTIC_LONG)
                .desc("Continue even when an unknown diagnostic is found during a mending iteration")
                .build())
            .addOption(Option.builder(REPORT_PER_SOURCE_SHORT)
                .longOpt(REPORT_PER_SOURCE_LONG)
                .desc("Create a report per source")
                .build())
            .addOption(Option.builder(OUTPUT_SHORT)
                .longOpt(OUTPUT_LONG)
                .desc("Output directory")
                .argName("path")
                .hasArg()
                .optionalArg(true)
                .type(String.class)
                .build())
            .addOption(Option.builder(OUTPUT_DIAGS_SHORT)
                .longOpt(OUTPUT_DIAGS_LONG)
                .desc("Output diagnostics")
                .build())
            .addOption(Option.builder(MENDFILE_FILENAME_SHORT)
                .longOpt(MENDFILE_FILENAME_LONG)
                .desc("Mendfile filename")
                .argName("filename")
                .hasArg()
                .optionalArg(true)
                .type(String.class)
                .build())
            .addOption(Option.builder(DIAGS_OUTPUT_FILENAME_SHORT)
                .longOpt(DIAGS_OUTPUT_FILENAME_LONG)
                .desc("Diagnostics output filename")
                .argName("filename")
                .hasArg()
                .optionalArg(true)
                .type(String.class)
                .build())
            .addOption(Option.builder(REPORT_FILENAME_SHORT)
                .longOpt(REPORT_FILENAME_LONG)
                .desc("Report filename")
                .argName("filename")
                .hasArg()
                .optionalArg(true)
                .type(String.class)
                .build())
            .addOption(Option.builder(SOURCE_REPORT_FILENAME_SHORT)
                .longOpt(SOURCE_REPORT_FILENAME_LONG)
                .desc("Source report filename")
                .argName("filename")
                .hasArg()
                .optionalArg(true)
                .type(String.class)
                .build())
            .addOption(Option.builder(VERBOSE_SHORT)
                .longOpt(VERBOSE_LONG)
                .desc("Use verbose output")
                .build())
            .addOption(Option.builder(THREADS_SHORT)
                .longOpt(THREADS_LONG)
                .desc("Number of threads to use")
                .argName("number")
                .hasArg()
                .optionalArg(true)
                .type(Integer.class)
                .build());

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

                if (!cmd.hasOption(REPORT_PER_SOURCE_SHORT) && cmd.hasOption(SOURCE_REPORT_FILENAME_SHORT)) {
                    Logging.FILE_LOGGER.warn("Source report filename specified but report per source not enabled");
                    CliReporting.warning("Source report filename specified but report per source not enabled");
                }
            }

            // todo check why one - also works for the long options
            // TODO the defaults on the builder should be used instead of the ones here. e.g., in the output we shouldn't have to specify again "./output" when we already have it as default in the builder
            return CMenderInvocation.builder()
                    .invocation(List.of(args))
                    .version(cmd.hasOption(VERSION_LONG))
                    .help(cmd.hasOption(HELP_SHORT))
                    .verbose(cmd.hasOption(VERBOSE_SHORT))
                    .threads(Integer.parseInt(cmd.getOptionValue(THREADS_SHORT, "1")))
                    .diagExporterPath(cmd.getOptionValue(DIAG_EXPORTER_SHORT))

                    // Flags to control the creation of copies and reports
                    .createMendfileCopyPerIteration(cmd.hasOption(MENDFILE_COPY_PER_ITERATION_SHORT))
                    .createDiagsOutputCopyPerIteration(cmd.hasOption(DIAGS_OUTPUT_COPY_PER_ITERATION_SHORT))
                    .createMendfileOnlyOnAlterations(cmd.hasOption(MENDFILE_ONLY_ON_ALTERATIONS_SHORT))
                    .continueOnUnknownDiagnostic(cmd.hasOption(CONTINUE_ON_UNKNOWN_DIAGNOSTIC_SHORT))
                    .reportPerSource(cmd.hasOption(REPORT_PER_SOURCE_SHORT))
                    .outputDiagsOutput(cmd.hasOption(OUTPUT_DIAGS_SHORT))

                    // Output
                    .output(cmd.getOptionValue(OUTPUT_SHORT, "./output"))

                    // Filenames
                    .mendfileFilename(addExtIfMissing(cmd.getOptionValue(MENDFILE_FILENAME_SHORT, "cmender_mends.h"), ".h"))
                    .diagsOutputFilename(addExtIfMissing(cmd.getOptionValue(DIAGS_OUTPUT_FILENAME_SHORT, "cmender_diags_output.json"), ".json"))
                    .reportFilename(addExtIfMissing(cmd.getOptionValue(REPORT_FILENAME_SHORT, "cmender_report.json"), ".json"))
                    .sourceReportFilename(addExtIfMissing(cmd.getOptionValue(SOURCE_REPORT_FILENAME_SHORT, "source_report.json"), ".json"))

                    .files(List.of(files))
                    .build();
        } catch (ParseException e) {
            throw new CliArgsParserException(e);
        }
    }

    private static String addExtIfMissing(String filename, String ext) {
        if (!filename.endsWith(ext)) {
            return filename + ext;
        }

        return filename;
    }
}
