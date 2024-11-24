package pt.up.fe.specs.cmender.mending;

import lombok.Builder;
import lombok.experimental.Accessors;
import pt.up.fe.specs.cmender.CMenderInvocation;

import java.util.List;

@Builder
@Accessors(fluent = true)
public record MendingTerminationStatus(
        TerminationType terminationType,
        double fileProgress,
        List<Integer> unknownDiags
) {
    public enum TerminationType {
        NO_TERMINATION,
        NO_MORE_ERRORS_OR_FATALS,
        DETECTED_CYCLE,
        UNKNOWN_DIAGNOSTIC
    }

    // TODO isContinueOnUnknownDiagnostic must also make it so you skip the unknown diagnostics
    //   or else it will just keep trying to mend the same unknown diagnostic
    public boolean finishedPrematurely(CMenderInvocation invocation) {
        return terminationType == TerminationType.UNKNOWN_DIAGNOSTIC && !invocation.isContinueOnUnknownDiagnostic()
                || terminationType == TerminationType.DETECTED_CYCLE;
    }

    public boolean success() {
        return terminationType == TerminationType.NO_MORE_ERRORS_OR_FATALS;
    }
}
