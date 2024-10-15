package pt.up.fe.specs.cmender.mending;

import lombok.Builder;

import pt.up.fe.specs.cmender.CMenderInvocation;
import pt.up.fe.specs.cmender.diag.DiagnosticID;

import java.util.List;

@Builder
public record DiagnosticMendResult(
        boolean success,
        boolean appliedMend,
        List<String> unknownDiags,
        List<DiagnosticID> mendedDiags
) {
    public boolean finishedPrematurely(CMenderInvocation invocation) {
        return !unknownDiags.isEmpty() && !invocation.isContinueOnUnknownDiagnostic();
    }
}
