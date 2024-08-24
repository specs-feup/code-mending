package pt.up.fe.specs.cmender.diag;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DiagnosticLevel {
    IGNORED,
    NOTE,
    REMARK,
    WARNING,
    ERROR,
    FATAL;

    @JsonValue
    public String getAsString() {
        return this.name().toLowerCase();
    }
}
