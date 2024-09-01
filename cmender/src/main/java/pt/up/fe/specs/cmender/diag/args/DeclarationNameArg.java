package pt.up.fe.specs.cmender.diag.args;

public record DeclarationNameArg(
        String name

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.DECLARATION_NAME;
    }
}
