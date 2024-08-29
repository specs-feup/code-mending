package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;


@Getter
@ToString
@Accessors(fluent = true)
public class ArrayType implements Type {

    private Type elementType;

    private Integer size;

    public ArrayType(Type elementType) {
        this.elementType = elementType;
        this.size = null;
    }

    public ArrayType(Type elementType, Integer size) {
        this.elementType = elementType;
        this.size = size;
    }

    public void setElementType(Type type) {
        this.elementType = type;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getArrayPart() {
        return "[" + (size != null? size : "") + "]";
    }

    // For now we don't support complex types (e.g., arrays of pointers, pointers to arrays, etc.)
    @Override
    public String getName() {
        return elementType.getName() + getArrayPart();
    }

    @Override
    public String modifyVariable(String varName) {
        return elementType.modifyVariable(varName) + getArrayPart();
    }

    /*
    @Override
    public String getName() {
        BasicDataType basicDataType = (BasicDataType) getBaseType();

        return basicDataType.getName() + getBaseStrippedName();
    }*/

    /*
    @Override
    public String getBaseStrippedName() {
        System.out.println(size);
        return (elementType.isPtrType()? " " + elementType.getBaseStrippedName() : "") +
                getArrayPart() +
                (elementType.isArrayType()? elementType.getBaseStrippedName() : "");
    }

    @Override
    public Type getBaseType() {
        return elementType.getBaseType();
    }*/

    @Override
    public boolean isCompositeDataType() {
        return true;
    }

    @Override
    public boolean isArrayType() {
        return true;
    }
}
