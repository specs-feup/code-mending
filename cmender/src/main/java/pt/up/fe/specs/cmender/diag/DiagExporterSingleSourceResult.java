package pt.up.fe.specs.cmender.diag;

import java.util.List;

public record DiagExporterSingleSourceResult(
        String file,
        int totalDiagsCount,
        int ignoredCount,
        int noteCount,
        int remarkCount,
        int warningCount,
        int errorCount,
        int fatalCount,
        List<Diagnostic> diags
) {
    public boolean hasErrors() {
        return errorCount > 0 || fatalCount > 0;
    }

    public Diagnostic getFirstError() {
        return diags.stream()
                .filter(diagnostic -> diagnostic.level() == DiagnosticLevel.ERROR || diagnostic.level() == DiagnosticLevel.FATAL)
                .findFirst()
                .orElse(null);
    }
}
