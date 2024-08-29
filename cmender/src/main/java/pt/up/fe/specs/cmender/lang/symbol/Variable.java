package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
@Accessors(fluent = true)
public class Variable extends Symbol {

    private Type type;

    public Variable(String name, Type type) {
        super(name);
        this.type = type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String asDeclarationString() {
        return "extern " + asDefinitionString();
    }

    @Override
    public String asDefinitionString() {
        return type.modifyVariable(name) + ";";
    }
}
