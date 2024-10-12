package pt.up.fe.specs.cmender.lang.type;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import pt.up.fe.specs.cmender.lang.symbol.SymbolDependency;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "class"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BuiltinType.class, name = "builtin"),
        @JsonSubTypes.Type(value = PointerType.class, name = "pointer"),
        @JsonSubTypes.Type(value = ArrayType.class, name = "array"),
        @JsonSubTypes.Type(value = RecordType.class, name = "record"),
        @JsonSubTypes.Type(value = EnumType.class, name = "enum"),
        @JsonSubTypes.Type(value = FunctionType.class, name = "function"),
})
public interface Type extends SymbolDependency {
    TypeKind kind();

    default boolean isBuiltinType() {
        return false;
    }

    default boolean isDerivedType() {
        return false;
    }

    default boolean isVoid() {
        return false;
    }

    default boolean isIntegralType() {
        return false;
    }

    default boolean isFloatingPointType() {
        return false;
    }

    default boolean isBooleanType() {
        return false;
    }

    default boolean isArrayType() {
        return false;
    }

    default boolean isPointerType() {
        return false;
    }

    default boolean isTagType() {
        return false;
    }

    default boolean isRecordType() {
        return false;
    }

    default boolean isEnumType() {
        return false;
    }

    default boolean isFunctionType() {
        return false;
    }

    default boolean isTypedefType() {
        return false;
    }
}
