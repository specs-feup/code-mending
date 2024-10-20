package pt.up.fe.specs.cmender.lang.declContext;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "kind"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RecordDecl.class, name = "record"),
})
public interface DeclContext {
    DeclContextKind kind();
}
