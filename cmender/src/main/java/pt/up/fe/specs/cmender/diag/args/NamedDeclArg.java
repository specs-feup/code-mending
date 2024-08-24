package pt.up.fe.specs.cmender.diag.args;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class NamedDeclArg extends DiagnosticArg {
    private String idName;
    private String readableName;
    private String qualName;
}
