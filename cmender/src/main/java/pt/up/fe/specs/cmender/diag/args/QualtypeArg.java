package pt.up.fe.specs.cmender.diag.args;

public record QualtypeArg(
        Qual qual,
        Type type

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.QUALTYPE;
    }

    public record Qual(
            String spelling,
            boolean hasConst,
            boolean hasVolatile,
            boolean hasRestrict,
            boolean hasUnaligned
    ) { }

    public record Type(
            String name,
            String canonical,
            String desugared,
            String baseTypeName
    ) { }
}
