package pt.up.fe.specs.cmender.diag.args;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DiagnosticArgKind {
    STD_STRING,
    C_STRING,
    SINT,
    UINT,
    TOKEN_KIND,
    IDENTIFIER,
    ADDR_SPACE,
    QUAL,
    QUALTYPE,
    DECLARATION_NAME,
    NAMED_DECL,
    NESTED_NAME_SPEC,
    DECL_CONTEXT,
    QUALTYPE_PAIR,
    ATTR;

    @JsonValue
    public String getAsString() {
        return this.name().toLowerCase();
    }
}
