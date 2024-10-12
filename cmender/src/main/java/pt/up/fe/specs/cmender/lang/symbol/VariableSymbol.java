package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.lang.type.QualType;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.List;
import java.util.Set;

@Getter
@ToString
@Accessors(fluent = true)
public class VariableSymbol extends Symbol {

    private QualType qualType;

    // This is the only constructor that should be used because we should always set an initial type
    //  A variable without a type does not make sense
    public VariableSymbol(String name, QualType type) {
        super(name);
        this.qualType = type;
    }

    public void setQualType(QualType qualType) {
        this.qualType = qualType;
    }

    @Override
    public String asDeclarationString() {
        return "extern " + asDefinitionString();
    }

    @Override
    public String asDefinitionString() {
        return qualType.substituteTypeUsageId(name) + ";";
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        return qualType.getDirectDependencies(table);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        qualType.addDirectDependencies(dependencies, table);
    }
}
