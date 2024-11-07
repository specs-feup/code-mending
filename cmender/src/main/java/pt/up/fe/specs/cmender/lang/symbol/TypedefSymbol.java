package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.lang.type.QualType;
import pt.up.fe.specs.cmender.lang.type.TypeKind;
import pt.up.fe.specs.cmender.lang.type.TypedefType;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Accessors(fluent = true)
public class TypedefSymbol extends Symbol {

    private final TypedefType typedefType;

    private final Set<TypeKind> permittedTypes;

    public TypedefSymbol(String name, TypedefType typedefType, Set<TypeKind> permittedTypes) {
        super(name);
        this.typedefType = typedefType;
        this.permittedTypes = new HashSet<>(permittedTypes);
    }

    public TypedefSymbol(String name, TypedefType typedefType) {
        super(name);
        this.typedefType = typedefType;
        this.permittedTypes = new HashSet<>(List.of(
                TypeKind.BUILTIN,
                TypeKind.POINTER,
                TypeKind.ARRAY,
                TypeKind.RECORD,
                TypeKind.ENUM,
                TypeKind.TYPEDEF,
                TypeKind.FUNCTION
        ));
    }

    public void setAliasedType(QualType qualType) {
        typedefType.setAliasedType(qualType);
    }

    public void setPermittedTypes(Set<TypeKind> permittedTypes) {
        this.permittedTypes.clear();
        this.permittedTypes.addAll(permittedTypes);
    }

    public boolean canChangeAliasedType() {
        return !this.permittedTypes.isEmpty();
    }

    @Override
    public String asDeclarationString() {
        return "typedef " + typedefType.aliasedType().substituteTypeUsageId(name) + ";";
    }

    @Override
    public String asDefinitionString() {
        return asDeclarationString();
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        return typedefType.getDirectDependencies(table);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        // we dont add recursively the types of the struct members because we only care about the
        //  direct dependency (this symbol) to build a dependency graph
        dependencies.add(this);
    }
}
