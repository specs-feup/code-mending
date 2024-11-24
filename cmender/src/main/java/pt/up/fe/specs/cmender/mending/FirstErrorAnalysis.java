package pt.up.fe.specs.cmender.mending;

import pt.up.fe.specs.cmender.diag.DiagExporterSourceResult;
import pt.up.fe.specs.cmender.diag.DiagnosticID;

import java.util.List;

public class FirstErrorAnalysis implements DiagnosticAnalysis {

    @Override
    public MendingTerminationStatus checkTermination(DiagExporterSourceResult sourceResult, MendingTable mendingTable) {
        if (!sourceResult.hasErrorsOrFatals()) {
            return MendingTerminationStatus.builder()
                    .terminationType(MendingTerminationStatus.TerminationType.NO_MORE_ERRORS_OR_FATALS)
                    .fileProgress(1.0)
                    .build();
        }

        var firstError = sourceResult.getFirstErrorOrFatal();
        var fileProgress = (double) firstError.location().fileOffset() / (double) sourceResult.size();

        if (mendingTable.handledDiagnostics().contains(firstError)) {
            return MendingTerminationStatus.builder()
                    .terminationType(MendingTerminationStatus.TerminationType.DETECTED_CYCLE)
                    .fileProgress(fileProgress)
                    .build();
        }

        if (DiagnosticID.fromIntID(firstError.id()).isUnknown()) {
            return MendingTerminationStatus.builder()
                    .terminationType(MendingTerminationStatus.TerminationType.UNKNOWN_DIAGNOSTIC)
                    .fileProgress(fileProgress)
                    .unknownDiags(List.of(firstError.id()))
                    .build();
        }

        return MendingTerminationStatus.builder()
                .terminationType(MendingTerminationStatus.TerminationType.NO_TERMINATION)
                .fileProgress(fileProgress)
                .build();
    }

    @Override
    public List<Integer> selectDiagnostics(DiagExporterSourceResult sourceResult, MendingTable mendingTable) {
        var firstErrorIdx = sourceResult.getFirstOrFatalIdx();

        assert firstErrorIdx != null;

        return List.of(firstErrorIdx);
    }
}
