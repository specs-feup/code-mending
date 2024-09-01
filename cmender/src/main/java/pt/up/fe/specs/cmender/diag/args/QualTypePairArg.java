package pt.up.fe.specs.cmender.diag.args;

public record QualTypePairArg(

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.QUALTYPE_PAIR;
    }
}
