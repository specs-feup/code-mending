package pt.up.fe.specs.cmender.mending;

import pt.up.fe.specs.cmender.diag.DiagExporterSourceResult;

import java.util.List;

public interface DiagnosticAnalysis {

    MendingTerminationStatus checkTermination(DiagExporterSourceResult sourceResult, MendingTable mendingTable);

    List<Integer> selectDiagnostics(DiagExporterSourceResult sourceResult, MendingTable mendingTable);
}
