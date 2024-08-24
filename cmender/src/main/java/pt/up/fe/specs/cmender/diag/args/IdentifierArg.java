package pt.up.fe.specs.cmender.diag.args;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class IdentifierArg extends DiagnosticArg {
    private String name;
    private boolean isReserved;
    private boolean hadMacroDef;
    private boolean hasMacroDef;
    private boolean isFinalMacro;
    private boolean isModulesImport;
    private boolean isRestrictExpansion;
    private int builtinID;
    private TokenKindArg tokenID;
    private String interestingIdentifier;
    private boolean isCppKeyword;
    private boolean isCppOperatorKeyword;
}
