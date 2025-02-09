package pt.up.fe.specs.cmender.mending.analysis;

import pt.up.fe.specs.cmender.diag.DiagExporterSourceResult;
import pt.up.fe.specs.cmender.diag.DiagnosticID;
import pt.up.fe.specs.cmender.mending.MendingTable;
import pt.up.fe.specs.cmender.mending.MendingTerminationStatus;

import java.util.List;

public interface DiagnosticAnalysis {

    default MendingTerminationStatus checkTermination(DiagExporterSourceResult sourceResult, MendingTable mendingTable, String sourceFile) {
        if (!sourceResult.hasErrorsOrFatals()) {
            return MendingTerminationStatus.builder()
                    .terminationType(MendingTerminationStatus.TerminationType.NO_MORE_ERRORS_OR_FATALS)
                    .fileProgress(1.0)
                    .unknownDiags(List.of())
                    .build();
        }

        //var firstError = sourceResult.getFirstErrorOrFatal();
        var firstErrorIdx = sourceResult.getFirstOrFatalIdx();

        assert firstErrorIdx != null && firstErrorIdx >= 0;

        var firstError = sourceResult.diags().get(firstErrorIdx);

        //var fileProgress = (double) firstError.location().fileOffset() / (double) sourceResult.size();

        // To avoid errors in mendfile or other errors. otherwise we might get file progresses of >1.0 or other erroneous values
        var firstErrorInSource = sourceResult.getFirstErrorOrFatalInSource(sourceFile);

        var fileProgress = firstErrorInSource == null? mendingTable.fileProgress() : (double) firstErrorInSource.location().fileOffset() / (double) sourceResult.size();

        mendingTable.setFileProgress(fileProgress);

        if (mendingTable.handledDiagnostics().contains(firstError)) {
            return MendingTerminationStatus.builder()
                    .terminationType(MendingTerminationStatus.TerminationType.DETECTED_CYCLE)
                    .fileProgress(fileProgress)
                    .unknownDiags(List.of())
                    .build();
        }

        if (DiagnosticID.fromIntID(firstError.id()).isUnknown()) {
            return MendingTerminationStatus.builder()
                    .terminationType(MendingTerminationStatus.TerminationType.UNKNOWN_DIAGNOSTIC)
                    .fileProgress(fileProgress)
                    .unknownDiags(List.of(firstErrorIdx))
                    .build();
        }

        return MendingTerminationStatus.builder()
                .terminationType(MendingTerminationStatus.TerminationType.NO_TERMINATION)
                .fileProgress(fileProgress)
                .unknownDiags(List.of())
                .build();
    }

    List<Integer> selectDiagnostics(DiagExporterSourceResult sourceResult, MendingTable mendingTable);
}
