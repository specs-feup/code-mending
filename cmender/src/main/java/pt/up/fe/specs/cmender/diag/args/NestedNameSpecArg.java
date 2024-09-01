package pt.up.fe.specs.cmender.diag.args;

public record NestedNameSpecArg(

) implements DiagnosticArg {
    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.NESTED_NAME_SPEC;
    }
}
