package pt.up.fe.specs.cmender.diag.args;

public record TokenKindArg(
        String name,
        String spelling,
        String category

) implements DiagnosticArg {
    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.TOKEN_KIND;
    }
}
