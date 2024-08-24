package pt.up.fe.specs.cmender.diag.location;

public record DiagnosticSourceRange(
        SourceLocation begin,
        SourceLocation end,
        String encompassingCode
) { }
