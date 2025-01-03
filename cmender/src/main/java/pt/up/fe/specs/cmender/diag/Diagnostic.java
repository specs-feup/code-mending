package pt.up.fe.specs.cmender.diag;

import pt.up.fe.specs.cmender.diag.location.DiagnosticLocation;
import pt.up.fe.specs.cmender.diag.location.DiagnosticSourceRange;

import java.util.List;

public record Diagnostic (
        int id,
        String labelId,
        DiagnosticLevel level,
        String category,
        int group,
        DiagnosticDescription description,
        DiagnosticLocation location,
        List<DiagnosticSourceRange> sourceRanges,
        String codeSnippet
) {
    public boolean isIgnored() {
        return level == DiagnosticLevel.IGNORED;
    }

    public boolean isNote() {
        return level == DiagnosticLevel.NOTE;
    }

    public boolean isRemark() {
        return level == DiagnosticLevel.REMARK;
    }

    public boolean isWarning() {
        return level == DiagnosticLevel.WARNING;
    }

    public boolean isError() {
        return level == DiagnosticLevel.ERROR;
    }

    public boolean isFatal() {
        return level == DiagnosticLevel.FATAL;
    }

    public boolean isErrorOrFatal() {
        return isError() || isFatal();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Diagnostic other)) {
            return false;
        }
        return id == other.id
                && description.message().equals(other.description.message())
                && location.equals(other.location);
    }
}
