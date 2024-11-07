package pt.up.fe.specs.cmender.lang.type;

import pt.up.fe.specs.cmender.lang.symbol.Symbol;
import pt.up.fe.specs.cmender.lang.symbol.SymbolDependency;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record QualType(
        String typeAsString,
        String canonicalTypeAsString,
        String typeUsageInDecls,
        Qualifiers qual,
        Type type,
        LangAddressSpace langAddressSpace
) implements SymbolDependency {

    // TODO this might be needless if we no longer work with canonical types for all types
    public boolean isAliased() {
        return !typeAsString.equals(canonicalTypeAsString);
    }

    public Optional<TypeName> typeName() {
        if (isAliased()) {
            return Optional.of(TypeName.typedefAlias(typeAsString));
        }

        if (type.isBuiltinType()) {
            return Optional.of(TypeName.builtin(typeAsString));
        }

        if (type.isTagType()) {
            return Optional.of(TypeName.tag(typeAsString));
        }

        return Optional.empty();
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        return new HashSet<>(type.getDirectDependencies(table));
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        type.addDirectDependencies(dependencies, table);
    }

    public String substituteTypeUsageId(String replacement) {
        String mockIdentifier = "diag_exporter_id";

        return typeUsageInDecls.replace(mockIdentifier, replacement);
    }
}
