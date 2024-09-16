package pt.up.fe.specs.cmender.mending;

import lombok.Builder;

@Builder
public record DiagnosticMendResult(
        boolean success,
        boolean appliedMend,
        String unknownDiag) {

    public boolean finishedPrematurely() {
        return unknownDiag != null;
    }
}
