package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class Symbol {

    protected String name;

    public Symbol(String name) {
        this.name = name;
    }

    public abstract String asDeclarationString();

    public abstract String asDefinitionString();
}
