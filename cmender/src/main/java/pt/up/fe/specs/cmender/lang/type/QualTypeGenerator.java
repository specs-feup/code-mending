package pt.up.fe.specs.cmender.lang.type;

public class QualTypeGenerator {
    public static QualType intUnqualifiedType() {
        return new QualType(
                "int",
                "int",
                "int diag_exporter_id",
                Qualifiers.unqualified(),
                new BuiltinType(BuiltinType.BuiltinKind.INT, "int"),
                null);
    }
}
