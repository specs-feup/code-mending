package pt.up.fe.specs.cmender.diag.args;

public record StdStringArg(
        String string

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.STD_STRING;
    }
}
