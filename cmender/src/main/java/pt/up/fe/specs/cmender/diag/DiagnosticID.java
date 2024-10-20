package pt.up.fe.specs.cmender.diag;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
public enum DiagnosticID {
    // For unknown diagnostic IDs. Mostly used to raise errors
    UNKNOWN(-1),

    // When a file being included is not found in the include path
    // (e.g., #include <nonexistent.h> or #include "nonexistent.h")
    ERR_PP_FILE_NOT_FOUND(975),

    // When a function definition has for example 'enum E' or 'struct S' as return type
    //   but the type is not defined in the same translation unit
    ERR_FUNC_DEF_INCOMPLETE_RESULT(3309),

    // When a member is accessed but does not exist in the struct
    ERR_NO_MEMBER(3681),

    //
    ERR_TYPECHECK_CONVERT_INCOMPATIBLE(4598),

    // When a variable is declared with an incomplete type, that is a type that is not fully defined (e.g., 'struct S')
    ERR_TYPECHECK_DECL_INCOMPLETE_TYPE(4609),

    // TODO
    ERR_TYPECHECK_ILLEGAL_INCREMENT_DECREMENT(4617),

    // When a value has a type that is not a pointer but is being dereferenced
    ERR_TYPECHECK_INDIRECTION_REQUIRES_POINTER(4624),

    // When in a expression a
    ERR_TYPECHECK_INVALID_OPERANDS(4628),

    // Suggestion between member reference with -> or . (TODO this might loop)
    ERR_TYPECHECK_MEMBER_REFERENCE_SUGGESTION(4639),

    // When a variable or type name from typedef is used without being declared. the id is misleading for the typedef case
    ERR_UNDECLARED_VAR_USE(4694),
    ERR_UNDECLARED_VAR_USE_SUGGEST(4695),

    // When a function is used without being declared
    EXT_IMPLICIT_FUNCTION_DECL_C99(4912);

    private final int intID;

    private static final Map<Integer, DiagnosticID> ID_MAP;

    static {
        ID_MAP = Arrays
                    .stream(DiagnosticID.values())
                    .collect(Collectors.toMap(DiagnosticID::intID, diagnosticID -> diagnosticID));
    }

    DiagnosticID(int intID) {
        this.intID = intID;
    }

    public static DiagnosticID fromIntID(int id) {
        return ID_MAP.getOrDefault(id, UNKNOWN);
    }

    public int getIntID() {
        return intID;
    }

    public String getStringID() {
        return this.name().toLowerCase();
    }
}
