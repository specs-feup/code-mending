package pt.up.fe.specs.cmender.lang.symbol;

import java.util.List;

public interface Type extends SymbolDependency {

    String getName();

    // String getBaseStrippedName();

    // Type getBaseType();

    String modifyVariable(String varName);

    default boolean isBasicDataType() {
        return false;
    }

    default boolean isCompositeDataType() {
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

    default boolean isPtrType() {
        return false;
    }

    default boolean isTypedefType() {
        return false;
    }

    default boolean isStructType() {
        return false;
    }

    default boolean isFunctionPtrType() {
        return false;
    }
}
