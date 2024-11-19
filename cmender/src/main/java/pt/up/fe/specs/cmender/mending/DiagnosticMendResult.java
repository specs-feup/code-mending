package pt.up.fe.specs.cmender.mending;

import lombok.Builder;

import pt.up.fe.specs.cmender.CMenderInvocation;

import java.util.List;

@Builder
public record DiagnosticMendResult(
        boolean success,
        boolean appliedMend,
        List<Integer> unknownDiags,
        List<DiagnosticShortInfo> selectedDiags,
        boolean detectedCycle
) {
    // TODO isContinueOnUnknownDiagnostic must also make it so you skip the unknown diagnostics
    //   or else it will just keep trying to mend the same unknown diagnostic
    public boolean finishedPrematurely(CMenderInvocation invocation) {
        return !unknownDiags.isEmpty() && !invocation.isContinueOnUnknownDiagnostic();
    }
}
