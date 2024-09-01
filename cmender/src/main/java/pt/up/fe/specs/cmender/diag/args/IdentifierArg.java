package pt.up.fe.specs.cmender.diag.args;

public record IdentifierArg(
    String name,
    boolean isReserved,
    boolean hadMacroDef,
    boolean hasMacroDef,
    boolean isFinalMacro,
    boolean isModulesImport,
    boolean isRestrictExpansion,
    int builtinID,
    TokenID tokenID,
    String interestingIdentifier,
    boolean isCppKeyword,
    boolean isCppOperatorKeyword

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.IDENTIFIER;
    }

    public record TokenID(
            String name,
            String spelling,
            String category

    ) { }
}
