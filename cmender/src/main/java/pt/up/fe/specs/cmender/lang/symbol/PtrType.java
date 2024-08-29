package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
@Accessors(fluent = true)
public class PtrType implements Type {

    private Type pointeeType;

    public PtrType(Type pointeeType) {
        this.pointeeType = pointeeType;
    }

    public void setPointeeType(Type pointeeType) {
        this.pointeeType = pointeeType;
    }

    // For now we don't support complex types (e.g., arrays of pointers, pointers to arrays, etc.)
    @Override
    public String getName() {
        return pointeeType.getName() + " *";
    }

    @Override
    public String modifyVariable(String varName) {

        return pointeeType.getName() + " *" + varName;
    }

    /*
    @Override
    public String getName() {
        BasicDataType basicDataType = (BasicDataType) getBaseType();

        return basicDataType.getName() + " " + getBaseStrippedName();
    }

    @Override
    public String getBaseStrippedName() {
        var isPtrOfArray = isPtrOfArray();

        if (isPtrOfArray) {
            var numberOfPointersOfArrays = getNumberOfPointersOfArrays();
            var getArray = getArray();
            return "(" + "*".repeat(numberOfPointersOfArrays) + ")" + getArray.getBaseStrippedName();
        }

        return "*" + pointedType.getBaseStrippedName();
    }

    private boolean isPtrOfArray() {
        return pointedType.isArrayType() || isPtrOfArrayRec(pointedType);
    }

    private boolean isPtrOfArrayRec(Type type) {
        return type.isArrayType() || (type.isPtrType() && isPtrOfArrayRec(((PtrType) type).pointedType));
    }

    private int getNumberOfPointersOfArrays() {
        return 1 + getNumberOfPointersOfArraysRec(pointedType);
    }

    private int getNumberOfPointersOfArraysRec(Type type) {
        if (type.isPtrType()) {
            return 1 + getNumberOfPointersOfArraysRec(((PtrType) type).pointedType);
        } else if (type.isArrayType()) {
            return 0;
        } else {
            return 0;
        }
    }

    private Type getArray() {
        return pointedType.isArrayType() ? pointedType : getArrayRec(pointedType);
    }

    private Type getArrayRec(Type type) {
        return type.isArrayType() ? type : getArrayRec(((PtrType) type).pointedType);
    }

    @Override
    public Type getBaseType() {
        return pointedType.getBaseType();
    }
    */

    @Override
    public boolean isCompositeDataType() {
        return true;
    }

    @Override
    public boolean isPtrType() {
        return true;
    }
}

