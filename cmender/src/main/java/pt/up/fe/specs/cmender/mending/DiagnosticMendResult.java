package pt.up.fe.specs.cmender.mending;

import lombok.Builder;

import pt.up.fe.specs.cmender.CMenderInvocation;

import java.util.List;

@Builder
public record DiagnosticMendResult(
        boolean success,
        boolean appliedMend,
        boolean detectedCycle,
        List<Integer> selectedDiags,
        List<Integer> unknownDiags,
        double fileProgress
) {
    // TODO isContinueOnUnknownDiagnostic must also make it so you skip the unknown diagnostics
    //   or else it will just keep trying to mend the same unknown diagnostic
    public boolean finishedPrematurely(CMenderInvocation invocation) {
        return !unknownDiags.isEmpty() && !invocation.isContinueOnUnknownDiagnostic();
    }
}
