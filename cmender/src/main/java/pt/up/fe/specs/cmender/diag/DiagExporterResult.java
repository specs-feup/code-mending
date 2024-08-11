package pt.up.fe.specs.cmender.diag;

public record DiagExporterResult (
        int exitCode,
        String outputFilepath
) { }
