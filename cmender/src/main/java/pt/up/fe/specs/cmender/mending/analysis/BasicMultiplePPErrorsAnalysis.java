package pt.up.fe.specs.cmender.mending.analysis;

import pt.up.fe.specs.cmender.diag.DiagExporterSourceResult;
import pt.up.fe.specs.cmender.diag.DiagnosticID;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.List;

public class BasicMultiplePPErrorsAnalysis implements DiagnosticAnalysis {
    private final BasicFirstErrorAnalysis basicFirstErrorAnalysis;

    public BasicMultiplePPErrorsAnalysis() {
        basicFirstErrorAnalysis = new BasicFirstErrorAnalysis();
    }

    // This will remove further occurences, meaning the one that remains will be the first one that appears
    //  TODO this will probably change line progression a lot
    private void removeExactlyRepeatedDiagnostics(List<Integer> diagnostics, DiagExporterSourceResult sourceResult) {
        for (int i = 0; i < diagnostics.size() - 1; i++) {
            var diag = sourceResult.diags().get(diagnostics.get(i));

            for (int j = i + 1; j < diagnostics.size(); j++) {
                var otherDiag = sourceResult.diags().get(diagnostics.get(j));
                if (diag.description().message().equals(otherDiag.description().message())) {
                    diagnostics.remove(j);
                    j--;
                }
            }
        }
    }

    @Override
    public List<Integer> selectDiagnostics(DiagExporterSourceResult sourceResult, MendingTable mendingTable) {
        var missingPPFiles = sourceResult.getDiagIdxs(DiagnosticID.ERR_PP_FILE_NOT_FOUND);

        removeExactlyRepeatedDiagnostics(missingPPFiles, sourceResult);

        if (!missingPPFiles.isEmpty()) {
            return missingPPFiles;
        }

        return basicFirstErrorAnalysis.selectDiagnostics(sourceResult, mendingTable);
    }
}
