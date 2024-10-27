package pt.up.fe.specs.cmender.lang.type;

import lombok.Getter;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.lang.symbol.Symbol;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Accessors(fluent = true)
public class TypedefType implements Type {

    private final String name;

    private QualType aliasedType;

    public TypedefType(String name, QualType aliasedType) {
        this.name = name;
        this.aliasedType = aliasedType;
    }

    public void setAliasedType(QualType qualType) {
        this.aliasedType = qualType;
    }

    @Override
    public TypeKind kind() {
        return TypeKind.TYPEDEF;
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        /*var typedefSymbol = table.typedefs().get(name);

        // If the symbol is not found, it might mean it is already a declared type on the code (TODO is this correct?)
        if (typedefSymbol == null) {
            return new HashSet<>();
        }

        return new HashSet<>(List.of(typedefSymbol));*/

        var dependencies = new ArrayList<Symbol>();

        aliasedType.addDirectDependencies(dependencies, table);

        return new HashSet<>(dependencies);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        var typedefSymbol = table.typedefs().get(name);

        // If the symbol is not found, it might mean it is already a declared type on the code (TODO is this correct?)
        if (typedefSymbol == null) {
            return;
        }

        dependencies.add(typedefSymbol);
    }
}
