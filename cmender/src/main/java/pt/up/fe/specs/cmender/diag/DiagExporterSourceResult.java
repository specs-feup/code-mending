package pt.up.fe.specs.cmender.diag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record DiagExporterSourceResult(
        String file,
        long size,
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

    public List<Integer> getDiagIdxs(DiagnosticID id) {
        var diags = new ArrayList<Integer>();

        for (int i = 0; i < this.diags.size(); i++) {
            if (this.diags.get(i).id() == id.id()) {
                diags.add(i);
            }
        }

        return diags;
    }

    public List<Integer> getDiagIdxs(Set<DiagnosticID> ids) {
        var diags = new ArrayList<Integer>();

        for (int i = 0; i < this.diags.size(); i++) {
            if (ids.contains(DiagnosticID.fromIntID(this.diags.get(i).id()))) {
                diags.add(i);
            }
        }

        return diags;
    }
}
