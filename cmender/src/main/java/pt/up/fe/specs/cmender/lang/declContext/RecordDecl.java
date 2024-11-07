package pt.up.fe.specs.cmender.lang.declContext;

import pt.up.fe.specs.cmender.lang.type.RecordType;

public record RecordDecl(
        // TODO maybe change the name to 'tagKind' to "recordKind" because enums are tags too
        RecordType.RecordKind tagKind,
        String name
) implements DeclContext {

    @Override
    public DeclContextKind kind() {
        return DeclContextKind.RECORD;
    }
}
