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
    public boolean hasErrorsOrFatals() {
        return errorCount > 0 || fatalCount > 0;
    }

    public Diagnostic getFirstErrorOrFatal() {
        return diags.stream()
                .filter(Diagnostic::isErrorOrFatal)
                .findFirst()
                .orElse(null);
    }

    public Integer getFirstOrFatalIdx() {
        for (int i = 0; i < diags.size(); i++) {
            if (diags.get(i).isErrorOrFatal()) {
                return i;
            }
        }

        return null;
    }
}
