package pt.up.fe.specs.cmender.lang.type;

import pt.up.fe.specs.cmender.lang.symbol.Symbol;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record FunctionType(
        FunctionTypeKind functionKind,
        QualType returnType,
        List<QualType> paramTypes

) implements Type {

    public enum FunctionTypeKind {
        PROTO,
        NO_PROTO
    }

    @Override
    public TypeKind kind() {
        return TypeKind.FUNCTION;
    }

    @Override
    public boolean isDerivedType() {
        return true;
    }

    @Override
    public boolean isFunctionType() {
        return true;
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        var dependencies = new ArrayList<Symbol>();

        returnType.addDirectDependencies(dependencies, table);
        paramTypes.forEach(p -> p.addDirectDependencies(dependencies, table));

        return new HashSet<>(dependencies);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        returnType.addDirectDependencies(dependencies, table);
        paramTypes.forEach(p -> p.addDirectDependencies(dependencies, table));
    }
}
