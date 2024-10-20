package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@ToString
@Accessors(fluent = true)
public class EnumSymbol extends Symbol {

    private List<String> values;

    public EnumSymbol(String name, List<String> values) {
        super(name);
        this.values = values;
    }

    public EnumSymbol(String name) {
        this(name, new ArrayList<>());
    }

    public void addValue(String value) {
        this.values.add(value);
    }

    public void setValues(List<String> values) {
        this.values = new ArrayList<>(values);
    }

    @Override
    public String asDeclarationString() {
        return "enum " + name + ";";
    }

    @Override
    public String asDefinitionString() {
        return "enum " + name + " {" +
                values.stream()
                        .collect(Collectors.joining(",\n\t",
                                values.isEmpty()? "" : "\n\t", values.isEmpty()? "" : "\n")) +
                "};";
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        // enum values are like "literals" or builtin types. they lack dependencies
        return new HashSet<>();
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        // enum values are like "literals" or builtin types. they lack dependencies
        //  direct dependency (this symbol) to build a dependency graph
        dependencies.add(this);
    }
}
