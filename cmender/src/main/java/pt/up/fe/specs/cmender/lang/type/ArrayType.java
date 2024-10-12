package pt.up.fe.specs.cmender.lang.type;

import com.fasterxml.jackson.annotation.JsonValue;
import pt.up.fe.specs.cmender.lang.symbol.Symbol;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.List;
import java.util.Set;

public record ArrayType(
        ArrayKind arrayKind,
        long size,
        Void sizeExpr, // TODO: Implement this
        QualType elementType
) implements Type {

    public enum ArrayKind {
        CONSTANT,
        VARIABLE,
        INCOMPLETE;

        @JsonValue
        public String getAsString() {
            return this.name().toLowerCase();
        }
    }

    @Override
    public TypeKind kind() {
        return TypeKind.ARRAY;
    }

    @Override
    public boolean isDerivedType() {
        return true;
    }

    @Override
    public boolean isArrayType() {
        return true;
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        return elementType.getDirectDependencies(table);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        elementType.addDirectDependencies(dependencies, table);
    }
}
