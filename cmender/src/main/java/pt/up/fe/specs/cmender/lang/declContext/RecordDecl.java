package pt.up.fe.specs.cmender.lang.declContext;

import pt.up.fe.specs.cmender.lang.type.RecordType;

public record RecordDecl(
        RecordType.RecordKind tagKind,
        String name
) implements DeclContext {

    @Override
    public DeclContextKind kind() {
        return DeclContextKind.RECORD;
    }
}
