package pt.up.fe.specs.cmender.diag.location;

import java.util.List;

public record DiagnosticLocation(
        DiagnosticLocationType type,
        SourceLocation presumedLoc,
        SourceLocation expansionLoc,
        List<SourceLocation> spellingLocs
) {
    public boolean isFileLoc() {
        return type == DiagnosticLocationType.FILE;
    }

    public boolean isMacroLoc() {
        return type == DiagnosticLocationType.MACRO;
    }
}
