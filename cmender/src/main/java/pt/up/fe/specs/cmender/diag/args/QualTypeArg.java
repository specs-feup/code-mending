package pt.up.fe.specs.cmender.diag.args;

import pt.up.fe.specs.cmender.lang.type.QualType;

public record QualTypeArg(
        QualType qualType

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.QUALTYPE;
    }
}
