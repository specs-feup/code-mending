package pt.up.fe.specs.cmender.diag;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
public enum DiagnosticID {
    EXT_IMPLICIT_FUNCTION_DECL_C99(4912),
    ERR_UNDECLARED_VAR_USE(4694),
    ERR_TYPECHECK_CONVERT_INCOMPATIBLE(4598),
    ERR_TYPECHECK_INVALID_OPERANDS(4628),
    ERR_PP_FILE_NOT_FOUND(975),
    UNKNOWN(-1);

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
}
