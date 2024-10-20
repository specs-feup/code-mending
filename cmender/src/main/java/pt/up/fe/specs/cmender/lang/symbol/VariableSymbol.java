package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.lang.type.QualType;
import pt.up.fe.specs.cmender.lang.type.TypedefType;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.List;
import java.util.Set;

@Getter
@ToString
@Accessors(fluent = true)
public class VariableSymbol extends Symbol {

    private final TypedefType type;

    // This is the only constructor that should be used because we should always set an initial type
    // A variable without a type does not make sense
    public VariableSymbol(String name, TypedefType type) {
        super(name);
        this.type = type;
    }

    public void setType(QualType qualType) {
        type.setAliasedType(qualType);
    }

    @Override
    public String asDeclarationString() {
        return "extern " + asDefinitionString();
    }

    @Override
    public String asDefinitionString() {
        return type.name() + " " + name() + ";";
        //return type.aliasedType().substituteTypeUsageId(name) + ";";
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        return type.getDirectDependencies(table);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        type.addDirectDependencies(dependencies, table);
    }
}
