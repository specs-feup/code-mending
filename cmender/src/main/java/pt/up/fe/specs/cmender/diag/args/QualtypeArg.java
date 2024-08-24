package pt.up.fe.specs.cmender.diag.args;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class QualtypeArg extends DiagnosticArg {
    public static class Type {
        private String name;
        private String canonical;
        private String desugared;
        private String baseTypeName;
    }

    private QualArg qual;
    private Type type;
}
