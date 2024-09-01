package pt.up.fe.specs.cmender.diag.args;

public record UIntArg(
        int integer

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.UINT;
    }
}
