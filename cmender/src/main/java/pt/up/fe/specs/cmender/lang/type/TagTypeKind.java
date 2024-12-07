package pt.up.fe.specs.cmender.lang.type;

public enum TagTypeKind {
    STRUCT,
    UNION,
    ENUM;

    public static TagTypeKind fromString(String tagKind) {
        return switch (tagKind) {
            case "struct" -> STRUCT;
            case "union" -> UNION;
            case "enum" -> ENUM;
            default -> throw new IllegalArgumentException("Unknown tag kind: " + tagKind);
        };
    }
}
