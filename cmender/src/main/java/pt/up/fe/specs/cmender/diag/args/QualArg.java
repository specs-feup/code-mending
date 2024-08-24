package pt.up.fe.specs.cmender.diag.args;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class QualArg extends DiagnosticArg {
    private String spelling;
    private boolean hasConst;
    private boolean hasVolatile;
    private boolean hasRestrict;
    private boolean hasUnaligned;
}
