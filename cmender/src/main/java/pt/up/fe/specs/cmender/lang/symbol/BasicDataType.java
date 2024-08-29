package pt.up.fe.specs.cmender.lang.symbol;

public enum BasicDataType implements Type {

    VOID,
    CHAR,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    _BOOL;

    @Override
    public String getName() {
        if (this.equals(_BOOL)) {
            return "_Bool";
        }

        return this.name().toLowerCase();
    }

    @Override
    public String modifyVariable(String varName) {
        return this.getName() + " " + varName;
    }

    /*
    @Override
    public String getBaseStrippedName() {
        return "";
    }

    @Override
    public Type getBaseType() {
        return this;
    }*/

    @Override
    public boolean isBasicDataType() {
        return true;
    }

    @Override
    public boolean isVoid() {
        return this.equals(VOID);
    }

    @Override
    public boolean isIntegralType() {
        return this.equals(CHAR) || this.equals(SHORT) || this.equals(INT) || this.equals(LONG);
    }

    @Override
    public boolean isFloatingPointType() {
        return this.equals(FLOAT) || this.equals(DOUBLE);
    }

    @Override
    public boolean isBooleanType() {
        return this.equals(_BOOL);
    }
}
