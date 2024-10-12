package pt.up.fe.specs.cmender.lang.symbol;

import pt.up.fe.specs.cmender.lang.type.QualType;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.List;
import java.util.Set;

public class TypedefSymbol extends Symbol {

    private QualType qualType;

    public TypedefSymbol(String name, QualType qualType) {
        super(name);
        this.qualType = qualType;
    }

    public void setType(QualType qualType) {
        this.qualType = qualType;
    }

    @Override
    public String asDeclarationString() {
        return "typedef " + qualType.substituteTypeUsageWithId(name) + ";";
    }

    @Override
    public String asDefinitionString() {
        return asDeclarationString();
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        return qualType.getDirectDependencies(table);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        // we dont add recursively the types of the struct members because we only care about the
        //  direct dependency (this symbol) to build a dependency graph
        dependencies.add(this);
    }
}
