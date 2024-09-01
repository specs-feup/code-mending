package pt.up.fe.specs.cmender.diag.args;

public record AttrArg(
        String spelling

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.ATTR;
    }
}
