package pt.up.fe.specs.cmender.diag.args;

import pt.up.fe.specs.cmender.lang.type.Qualifiers;

public record QualArg(
        Qualifiers qual

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.QUAL;
    }
}
