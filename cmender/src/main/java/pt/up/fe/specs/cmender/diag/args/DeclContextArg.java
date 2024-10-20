package pt.up.fe.specs.cmender.diag.args;

import pt.up.fe.specs.cmender.lang.declContext.DeclContext;

public record DeclContextArg(
        DeclContext declContext

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.DECL_CONTEXT;
    }
}
