package pt.up.fe.specs.cmender.lang.type;

import pt.up.fe.specs.cmender.lang.symbol.Symbol;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.List;
import java.util.Set;

public record PointerType(
        QualType pointeeType

) implements Type {

    @Override
    public TypeKind kind() {
        return TypeKind.POINTER;
    }

    @Override
    public boolean isDerivedType() {
        return true;
    }

    @Override
    public boolean isPointerType() {
        return true;
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        return pointeeType.getDirectDependencies(table);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        pointeeType.addDirectDependencies(dependencies, table);
    }
}
