package pt.up.fe.specs.cmender.mending;

import lombok.Builder;

import pt.up.fe.specs.cmender.CMenderInvocation;

@Builder
public record DiagnosticMendResult(
        boolean success,
        boolean appliedMend,
        String unknownDiag) {

    public boolean finishedPrematurely(CMenderInvocation invocation) {
        return unknownDiag != null && !invocation.isContinueOnUnknownDiagnostic();
    }
}
