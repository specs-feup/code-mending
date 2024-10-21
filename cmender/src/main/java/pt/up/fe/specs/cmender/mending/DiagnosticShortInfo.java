package pt.up.fe.specs.cmender.mending;

import lombok.Builder;

import pt.up.fe.specs.cmender.diag.Diagnostic;
import pt.up.fe.specs.cmender.diag.DiagnosticID;
import pt.up.fe.specs.cmender.diag.DiagnosticLevel;

// This DiagnosticShortInfo model one is supposed to be used for CMender results. For processing we use the Diagnostic class
@Builder
public record DiagnosticShortInfo(
        String id,
        DiagnosticLevel level,
        int line,
        int column,
        String message
) {
    // TODO we can also call this 'of' or 'to'
    public static DiagnosticShortInfo from(Diagnostic diag) {
        return DiagnosticShortInfo.builder()
                .id(DiagnosticID.fromIntID(diag.id()).getLabelID())
                .level(diag.level())
                .line(diag.location().isFileLoc()? diag.location().presumedLoc().line() : diag.location().expansionLoc().line()) // TODO verify if for macro loc we should use expansion loc
                .column(diag.location().isFileLoc()? diag.location().presumedLoc().column() : diag.location().expansionLoc().column()) // TODO verify if for macro loc we should use expansion loc
                .message(diag.description().message())
                .build();
    }
}
