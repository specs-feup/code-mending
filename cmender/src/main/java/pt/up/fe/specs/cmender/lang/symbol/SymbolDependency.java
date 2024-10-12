package pt.up.fe.specs.cmender.lang.symbol;

import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.List;
import java.util.Set;

public interface SymbolDependency {

    Set<Symbol> getDirectDependencies(MendingTable table);

    // this one is useful for aggregating dependencies (i.e., after getDirectDependencies has been called on one of the classes implementing this)
    void addDirectDependencies(List<Symbol> dependencies, MendingTable table);
}
