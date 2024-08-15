package pt.up.fe.specs.cmender.cli;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.fusesource.jansi.Ansi;

import pt.up.fe.specs.cmender.CMenderProperties;

public class CliReporting {
    private static final String WARNING_PREFIX = "warning: ";

    private static final String ERROR_PREFIX = "error: ";

    private static final String USAGE_STRING = "jar cmender.jar [options] file...";

    // TODO find in which terminals AnsiConsole.systemInstall(); is required

    public static void info(String message) {
        System.out.println(message);
    }

    public static void info(String message, Object... args) {
        info(String.format(message, args));
    }

    public static void warning(String message) {
        System.out.println(
                Ansi.ansi()
                        .fgYellow()
                        .bold()
                        .a(WARNING_PREFIX)
                        .reset()
                        .bold()
                        .a(message)
                        .reset());
    }

    public static void warning(String message, Object... args) {
        warning(String.format(message, args));
    }

    public static void error(String message) {
        System.out.println(
                Ansi.ansi()
                        .fgRed()
                        .bold()
                        .a(ERROR_PREFIX)
                        .reset()
                        .bold()
                        .a(message)
                        .reset());
    }

    public static void error(String message, Object... args) {
        error(String.format(message, args));
    }

    public static void help(CMenderProperties properties, Options options) {
        var helpFormatter = new HelpFormatter();
        CliReporting.info("overview: %s - %s by %s\n", properties.name(), properties.description(), properties.vendor());
        helpFormatter.printHelp(USAGE_STRING, options);
    }

    public static void usage(CMenderProperties properties, Options options) {
        var helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(USAGE_STRING, options);
    }

    public static void version(CMenderProperties properties) {
        CliReporting.info(properties.name() + " by " + properties.vendor());
        CliReporting.info("Version: %s", properties.version());
        CliReporting.info("Release Date: %s", properties.date());
    }
}
