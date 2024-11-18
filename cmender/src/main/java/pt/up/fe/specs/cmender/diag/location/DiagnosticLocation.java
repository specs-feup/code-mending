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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DiagnosticLocation other)) {
            return false;
        }

        if (type != other.type) {
            return false;
        }

        if (isFileLoc()) {
            return presumedLoc.equals(other.presumedLoc);
        }

        return expansionLoc().equals(other.expansionLoc); // TODO revise if we should compare spelling locs
    }
}
