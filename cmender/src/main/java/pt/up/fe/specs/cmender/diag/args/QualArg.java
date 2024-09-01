package pt.up.fe.specs.cmender.diag.args;

public record QualArg(
        String spelling,
        boolean hasConst,
        boolean hasVolatile,
        boolean hasRestrict,
        boolean hasUnaligned

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.QUAL;
    }
}
