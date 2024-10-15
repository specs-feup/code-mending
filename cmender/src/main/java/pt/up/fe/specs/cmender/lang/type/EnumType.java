package pt.up.fe.specs.cmender.lang.type;

import lombok.Getter;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.lang.symbol.Symbol;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Accessors(fluent = true)
public class EnumType implements Type {

    private final String name;

    // Reference to the symbol that represents this enum type
    // private EnumSymbol enumSymbol;

    public EnumType(String name) {
        this.name = name;
    }

    /*public void setRecordSymbol(EnumSymbol enumSymbol) {
        this.enumSymbol = enumSymbol;
    }*/

    @Override
    public TypeKind kind() {
        return TypeKind.ENUM;
    }

    @Override
    public boolean isDerivedType() {
        return true;
    }

    @Override
    public boolean isTagType() {
        return true;
    }

    @Override
    public boolean isEnumType() {
        return true;
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        var enumSymbol = table.enums().get(name);

        if (enumSymbol == null) {
            return new HashSet<>();
        }

        return new HashSet<>(List.of(enumSymbol));
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        var enumSymbol = table.structs().get(name);

        if (enumSymbol == null) {
            return;
        }

        dependencies.add(enumSymbol);
    }
}