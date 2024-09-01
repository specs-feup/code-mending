package pt.up.fe.specs.cmender.diag.args;

public record SIntArg(
        int integer

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.SINT;
    }
}
