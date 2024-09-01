package pt.up.fe.specs.cmender.diag.args;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "kind"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StdStringArg.class, name = "std_string"),
        @JsonSubTypes.Type(value = CStringArg.class, name = "c_string"),
        @JsonSubTypes.Type(value = SIntArg.class, name = "sint"),
        @JsonSubTypes.Type(value = UIntArg.class, name = "uint"),
        @JsonSubTypes.Type(value = TokenKindArg.class, name = "token_kind"),
        @JsonSubTypes.Type(value = IdentifierArg.class, name = "identifier"),
        @JsonSubTypes.Type(value = AddrSpaceArg.class, name = "addr_space"),
        @JsonSubTypes.Type(value = QualArg.class, name = "qual"),
        @JsonSubTypes.Type(value = QualtypeArg.class, name = "qualtype"),
        @JsonSubTypes.Type(value = DeclarationNameArg.class, name = "declaration_name"),
        @JsonSubTypes.Type(value = NamedDeclArg.class, name = "named_decl"),
        @JsonSubTypes.Type(value = NestedNameSpecArg.class, name = "nested_name_spec"),
        @JsonSubTypes.Type(value = DeclContextArg.class, name = "decl_context"),
        @JsonSubTypes.Type(value = QualTypePairArg.class, name = "qualtype_pair"),
        @JsonSubTypes.Type(value = AttrArg.class, name = "attr"),

})
public interface DiagnosticArg {
    DiagnosticArgKind kind();
}
