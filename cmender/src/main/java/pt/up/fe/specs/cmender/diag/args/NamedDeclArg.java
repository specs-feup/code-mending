package pt.up.fe.specs.cmender.diag.args;

public record NamedDeclArg(
        String idName,
        String readableName,
        String qualName

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.NAMED_DECL;
    }
}
