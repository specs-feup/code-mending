package pt.up.fe.specs.cmender.diag;

import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public class DiagExporterException extends Exception {
    private final Integer exitCode;
    private final String processOutput;

    private DiagExporterException(String message, Integer exitCode, String processOutput) {
        super(message);
        this.exitCode = exitCode;
        this.processOutput = processOutput;
    }

    private DiagExporterException(String message, Throwable cause) {
        super(message, cause);
        this.exitCode = null;
        this.processOutput = null;
    }

    // TODO improve with error codes (add more error granularity in diag-exporter)
    public static DiagExporterException ofBadProcessExitCode(int exitCode, String processOutput) {
        // TODO remove this (diag-exporter no longer outputs this message)
        var fileAlreadyExistsPattern = Pattern.compile("^(could not save results to output file; file already exists: .+)$",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

        var matcher = fileAlreadyExistsPattern.matcher(processOutput);

        var reason = matcher.find()? ": " + matcher.group() : "";

        return new DiagExporterException(
                String.format("diag-exporter process exited with code %d%s", exitCode, reason),
                    exitCode, processOutput);
    }

    public static DiagExporterException ofProcessException(Throwable cause) {
        return new DiagExporterException("a diag-exporter process exception occurred", cause);
        //return new DiagExporterException(
        //        String.format("diag-exporter process exception: %s", cause.getMessage()), cause);
    }
}
