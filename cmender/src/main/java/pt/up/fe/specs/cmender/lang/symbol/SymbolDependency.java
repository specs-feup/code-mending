package pt.up.fe.specs.cmender.lang.symbol;

import java.util.List;
import java.util.Set;

public interface SymbolDependency {

    Set<Symbol> getDirectDependencies();

    void addDirectDependencies(List<Symbol> dependencies);
}
