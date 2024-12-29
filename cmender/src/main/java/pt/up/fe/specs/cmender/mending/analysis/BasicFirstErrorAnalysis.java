package pt.up.fe.specs.cmender.mending.analysis;

import pt.up.fe.specs.cmender.diag.DiagExporterSourceResult;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.List;

public class BasicFirstErrorAnalysis implements DiagnosticAnalysis {
    @Override
    public List<Integer> selectDiagnostics(DiagExporterSourceResult sourceResult, MendingTable mendingTable) {
        var firstErrorIdx = sourceResult.getFirstOrFatalIdx();

        assert firstErrorIdx != null;

        return List.of(firstErrorIdx);
    }
}
