package pt.up.fe.specs.cmender.diag.location;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DiagnosticLocationType {
    NONE,
    FILE,
    MACRO;

    @JsonValue
    public String getAsString() {
        return this.name().toLowerCase();
    }
}
