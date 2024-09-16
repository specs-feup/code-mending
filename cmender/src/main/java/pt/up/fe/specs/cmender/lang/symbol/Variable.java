package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

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

    @Override
    public Set<Symbol> getDirectDependencies() {
        return type.getDirectDependencies();
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies) {
        type.addDirectDependencies(dependencies);
    }
}
