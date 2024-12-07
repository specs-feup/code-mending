package pt.up.fe.specs.cmender.lang.type;

public record TypeName(
        String identifier,
        // for builtin types and typedef aliases, this is the same as 'identifier'. For tags, it's in the form 'tagKind typeName'
        String typeName,
        TypeNameKind kind,
        TagTypeKind tagKind
) {
    public boolean isBuiltin() {
        return kind == TypeNameKind.BUILTIN;
    }

    public boolean isTag() {
        return kind == TypeNameKind.TAG;
    }

    public boolean isTypedefAlias() {
        return kind == TypeNameKind.TYPEDEF_ALIAS;
    }

    public enum TypeNameKind {
        BUILTIN,
        TAG,
        TYPEDEF_ALIAS;
    }

    public static TypeName builtin(String typeName) {
        return new TypeName(typeName, typeName, TypeNameKind.BUILTIN, null);
    }

    public static TypeName tag(String typeName) {
        var parts = typeName.split(" ");

        assert parts.length == 2;

        return new TypeName(parts[1], typeName, TypeNameKind.TAG, TagTypeKind.fromString(parts[0]));
    }

    public static TypeName typedefAlias(String typeName) {
        return new TypeName(typeName, typeName, TypeNameKind.TYPEDEF_ALIAS, null);
    }
}
