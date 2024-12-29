package pt.up.fe.specs.cmender.cli;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

public class CliArgsParserException extends Exception {
    public CliArgsParserException(String message) {
        super(message);
    }

    public CliArgsParserException(ParseException e) {
        super(adaptMessage(e));
    }

    public static CliArgsParserException ofMissingOption(String option) {
        return new CliArgsParserException(String.format("missing option: '%s'", option));
    }

    public static CliArgsParserException ofOptionMissingArgument(String option) {
        return new CliArgsParserException(String.format("missing argument for option: '%s'", option));
    }

    public static CliArgsParserException ofMissingInputFiles() {
        return new CliArgsParserException("no input files specified");
    }

    public static CliArgsParserException ofInvalidAnalysisType(String analysisType) {
        return new CliArgsParserException(String.format("invalid analysis type: '%s'", analysisType));
    }

    public static CliArgsParserException ofInvalidHandlerType(String handlerType) {
        return new CliArgsParserException(String.format("invalid handler type: '%s'", handlerType));
    }

    private static String adaptMessage(ParseException e) {
        if (e instanceof UnrecognizedOptionException unrecognizedOptionException) {
            return String.format("unknown option: '%s'", unrecognizedOptionException.getOption());
        }

        if (e instanceof AlreadySelectedException) {
            // TODO think if this requires improvement (more customized)
            return e.getMessage().substring(0, 1).toUpperCase() + e.getMessage().substring(1);
        }

        if (e instanceof MissingArgumentException missingArgumentException) {
            return String.format("missing argument for option: '%s'", missingArgumentException.getOption().getKey());
        }

        if (e instanceof MissingOptionException missingOptionException) {
            // TODO think if this requires improvement (more customized)
            return String.format("no options: %s", missingOptionException.getMissingOptions());
        }

        return "";
    }
}
