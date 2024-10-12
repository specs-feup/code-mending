package pt.up.fe.specs.cmender.lang.type;

import pt.up.fe.specs.cmender.lang.symbol.Symbol;
import pt.up.fe.specs.cmender.lang.symbol.SymbolDependency;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record QualType(
        String typeAsString,
        String canonicalTypeAsString,
        String typeUsageInDecls,
        Qualifiers qual,
        Type type,
        LangAddressSpace langAddressSpace
) implements SymbolDependency {
    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        return new HashSet<>(type.getDirectDependencies(table));
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        type.addDirectDependencies(dependencies, table);
    }

    public String substituteTypeUsageWithId(String replacement) {
        // TODO check for functions returning function pointers
        //  syntax: int (*get_operation(char op))(int, int);
        String mockIdentifier = "diag_exporter_id";

        return typeUsageInDecls.replace(mockIdentifier, replacement);
    }
}
