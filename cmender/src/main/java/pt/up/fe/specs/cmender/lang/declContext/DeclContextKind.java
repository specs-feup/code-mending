package pt.up.fe.specs.cmender.lang.declContext;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DeclContextKind {
    RECORD;

    @JsonValue
    public String getAsString() {
        return this.name().toLowerCase();
    }
}
