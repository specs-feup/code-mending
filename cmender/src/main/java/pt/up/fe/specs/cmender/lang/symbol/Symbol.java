package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public abstract class Symbol implements SymbolDependency {

    protected String name;

    public Symbol(String name) {
        this.name = name;
    }

    public abstract String asDeclarationString();

    public abstract String asDefinitionString();
}
