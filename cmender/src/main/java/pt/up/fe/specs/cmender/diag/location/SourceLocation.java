package pt.up.fe.specs.cmender.diag.location;

public record SourceLocation(
        int line,
        int column,
        String file,
        String path,
        String encompassingCode
) { }
