package pt.up.fe.specs.cmender.cli;

import org.fusesource.jansi.Ansi;

public class CliReporting {
    private static final String WARNING_PREFIX = "warning: ";

    private static final String ERROR_PREFIX = "error: ";

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
}
