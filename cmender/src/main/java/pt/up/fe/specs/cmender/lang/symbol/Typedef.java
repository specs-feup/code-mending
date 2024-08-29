package pt.up.fe.specs.cmender.lang.symbol;

public class Typedef extends Symbol implements Type {
    private Type type;

    public Typedef(String name, Type type) {
        super(name);
        this.type = type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String asDeclarationString() {
        var typeStrLeft = type.isArrayType()?  ((ArrayType)type).elementType().getName() : type.getName();
        var typeStrRight = type.isArrayType()? ((ArrayType)type).getArrayPart() : "";
        return "typedef " + typeStrLeft + (type.isPtrType()? "" : " ") + name + typeStrRight + ";";
    }

    @Override
    public String asDefinitionString() {
        return asDeclarationString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String modifyVariable(String varName) {
        return name + " " + varName;
    }

    @Override
    public boolean isCompositeDataType() {
        return true;
    }

    @Override
    public boolean isTypedefType() {
        return true;
    }
}
