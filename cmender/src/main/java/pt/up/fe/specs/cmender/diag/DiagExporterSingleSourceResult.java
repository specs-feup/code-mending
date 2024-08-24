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
) { }
