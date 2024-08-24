package pt.up.fe.specs.cmender.diag;

import pt.up.fe.specs.cmender.diag.location.DiagnosticLocation;
import pt.up.fe.specs.cmender.diag.location.DiagnosticSourceRange;

import java.util.List;

public record Diagnostic (
        int id,
        String description,
        DiagnosticLevel level,
        String category,
        int group,
        DiagnosticMessage message,
        DiagnosticLocation location,
        List<DiagnosticSourceRange> sourceRanges,
        String codeSnippet
) { }
