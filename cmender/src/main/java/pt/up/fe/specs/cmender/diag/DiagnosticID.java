package pt.up.fe.specs.cmender.diag;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Getter
@Accessors(fluent = true)
public enum DiagnosticID {
    EXT_IMPLICIT_FUNCTION_DECL_C99(4912),
    ERR_UNDECLARED_VAR_USE(4694),
    UNKNOWN(-1);

    private final int intID;

    private static final Map<Integer, DiagnosticID> ID_MAP;

    static {
        ID_MAP = new HashMap<>();
        for (DiagnosticID diagnosticID : DiagnosticID.values()) {
            // TODO can we add unknown to the map?
            if (diagnosticID == UNKNOWN) {
                continue;
            }

            ID_MAP.put(diagnosticID.intID, diagnosticID);
        }
    }

    DiagnosticID(int intID) {
        this.intID = intID;
    }

    public static DiagnosticID fromIntID(int id) {
        /*for (var value : values()) {
            if (value.intID == id) {
                return value;
            }
        }*/

        return ID_MAP.getOrDefault(id, UNKNOWN);
        // return null;
        //throw new IllegalArgumentException("Unknown diagnostic ID: " + id);
    }
}
