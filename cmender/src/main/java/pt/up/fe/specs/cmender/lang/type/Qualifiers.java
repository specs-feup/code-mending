package pt.up.fe.specs.cmender.lang.type;

public record Qualifiers(
        String spelling,
        boolean hasQualifiers,
        boolean hasConst,
        boolean hasVolatile,
        boolean hasRestrict,
        boolean hasUnaligned
) {

    public static Qualifiers unqualified() {
        return new Qualifiers(
                "",
                false,
                false,
                false,
                false,
                false);
    }
}
