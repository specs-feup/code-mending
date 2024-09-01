package pt.up.fe.specs.cmender.diag.args;

public record CStringArg(
        String string

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.C_STRING;
    }
}
