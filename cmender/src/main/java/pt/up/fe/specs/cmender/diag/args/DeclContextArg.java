package pt.up.fe.specs.cmender.diag.args;

public record DeclContextArg(

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.DECL_CONTEXT;
    }
}
