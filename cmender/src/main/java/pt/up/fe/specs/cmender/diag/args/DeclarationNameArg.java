package pt.up.fe.specs.cmender.diag.args;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class DeclarationNameArg extends DiagnosticArg {
    private String nName;
}
