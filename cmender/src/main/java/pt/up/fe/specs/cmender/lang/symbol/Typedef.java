package pt.up.fe.specs.cmender.lang.symbol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Typedef extends Symbol implements Type, SymbolDependency {
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

    /*@Override
    public List<Symbol> getDependencies() {
        return type.getDirectSymbolDependencies();
    }

    @Override
    public void getDependencies(List<Symbol> dependencies) {
        dependencies.addAll(type.getDirectSymbolDependencies());
    }*/

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String modifyVariable(String varName) {
        return name + " " + varName;
    }

    /*@Override
    public List<Symbol> getDirectSymbolDependencies() {
        return type.getDirectSymbolDependencies();
    }

    @Override
    public void updateDirectSymbolDependencies(List<Symbol> dependencies) {
        type.updateDirectSymbolDependencies(dependencies);
    }*/

    @Override
    public boolean isCompositeDataType() {
        return true;
    }

    @Override
    public boolean isTypedefType() {
        return true;
    }

    @Override
    public Set<Symbol> getDirectDependencies() {
        var dependencies = new ArrayList<Symbol>();
        type.addDirectDependencies(dependencies);
        return new HashSet<>(dependencies);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies) {
        dependencies.add(this);
    }
}
