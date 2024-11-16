package pt.up.fe.specs.cmender.mending;

// The names generated by this class are used when creating types for symbols that have no type information yet
//     (i.e., symbols that are not declared in the code, but are used in the code)
public class MendingTypeNameGenerator {
    private static final String CMENDER_TYPEDEF_ALIAS_NAME_PREFIX = "cmender_type_alias_";

    private static final String CMENDER_TAG_TYPE_NAME_PREFIX = "cmender_tag_type_";

    private static int typedefAliasNameCounter = 0;

    private static int tagTypeNameCounter = 0;

    // Names generated by this function should only be used as type names for aliases of other types (i.e., typedefs)
    // Additionally, each of these names should only be used for one symbol (i.e., for each variable, function, struct member),
    //     meaning that with the name we can also uniquely identify the symbol.
    // This is relevant and necessary because this way, we can easily replace the type of the symbol in the code by only looking
    // at the diagnostic messages with type mismatch information
    public static String newTypedefAliasName() {
        return CMENDER_TYPEDEF_ALIAS_NAME_PREFIX + typedefAliasNameCounter++;
    }

    public static String newTagTypeName() {
        return CMENDER_TAG_TYPE_NAME_PREFIX + tagTypeNameCounter++;
    }

    public static boolean isGeneratedName(String name) {
        return name.startsWith(CMENDER_TYPEDEF_ALIAS_NAME_PREFIX) || name.startsWith(CMENDER_TAG_TYPE_NAME_PREFIX);
    }

    public static boolean isGeneratedTypedefAliasName(String name) {
        return name.startsWith(CMENDER_TYPEDEF_ALIAS_NAME_PREFIX);
    }

    public static boolean isGeneratedTagTypeName(String name) {
        return name.startsWith(CMENDER_TAG_TYPE_NAME_PREFIX);
    }
}