package pt.up.fe.specs.cmender.mending;

import pt.up.fe.specs.cmender.diag.Diagnostic;
import pt.up.fe.specs.cmender.diag.args.DeclarationNameArg;
import pt.up.fe.specs.cmender.diag.args.DiagnosticArgKind;
import pt.up.fe.specs.cmender.diag.args.IdentifierArg;
import pt.up.fe.specs.cmender.diag.args.QualTypeArg;
import pt.up.fe.specs.cmender.lang.symbol.FunctionSymbol;
import pt.up.fe.specs.cmender.lang.symbol.RecordSymbol;
import pt.up.fe.specs.cmender.lang.symbol.TypedefSymbol;
import pt.up.fe.specs.cmender.lang.symbol.VariableSymbol;
import pt.up.fe.specs.cmender.lang.type.LangAddressSpace;
import pt.up.fe.specs.cmender.lang.type.QualType;
import pt.up.fe.specs.cmender.lang.type.Qualifiers;
import pt.up.fe.specs.cmender.lang.type.RecordType;
import pt.up.fe.specs.cmender.lang.type.Type;

public class MendingHandlers {

    private static final String UNNAMED_SYMBOL_NAME_PREFIX = "cmender_unnamed_symbol_";

    private static int unnamedSymbolCounter = 0;

    private static String newName() {
        return UNNAMED_SYMBOL_NAME_PREFIX + unnamedSymbolCounter++;
    }

    // When we don't know a symbol's type, we create a new unnamed type (mock type), which will be replaced later
    // This mock type is a struct with no fields because it will be more likely to be replaced by a real type
    //   than a type like int or float, which get easily converted to other numeric types, changing some
    //   semantics of the original program
    // TODO should we also use a typedef to reference this struct?
    private static QualType createNewUnnamedStartingQualType(MendingTable mendingTable) {
        var structSymbol = new RecordSymbol(newName());

        //var structType = new RecordType(structSymbol.name(), RecordType.RecordKind.STRUCT, structSymbol);
        var structType = new RecordType(structSymbol.name(), RecordType.RecordKind.STRUCT);

        var qualType = new QualType(
                "struct " + structSymbol.name(),
                "struct " + structSymbol.name(),
                "struct " + structSymbol.name() + " diag_exporter_id",
                Qualifiers.unqualified(),
                structType,
                null
        );

        mendingTable.structs().put(structSymbol.name(), structSymbol);

        return qualType;
        //var typedef = new TypedefSymbol(newName(), qualType);
        //mendingTable.typedefs().put(typedef.name(), typedef);
        //return typedef;
    }

    // TODO find if we can have the handlers as objects (also they can be placed in the enum DiagnosticID)
    //  or in terms of performance it is better to have them as static methods
    public static void handleExtImplicitFunctionDeclC99(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("C99 implicit function declaration");

        var functionName = ((IdentifierArg) diag.message().args().getFirst()).name();

        var function = new FunctionSymbol(functionName);

        mendingTable.functions().put(functionName, function);
    }

    public static void handleErrUndeclaredVarUse(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Undeclared variable use");

        var varName = ((DeclarationNameArg) diag.message().args().getFirst()).name();

        var variable = new VariableSymbol(varName, createNewUnnamedStartingQualType(mendingTable));

        mendingTable.variables().put(varName, variable);
    }

    public static void handleErrTypecheckConvertIncompatible(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Incompatible type conversion");

        var codeSnippet = diag.codeSnippet();

        // TODO for now we assume the lhs is a variable, but it can also be e.g., a struct member
        var equalsLhs = codeSnippet.substring(0, codeSnippet.indexOf("=")).trim();

        // For now we assume that the file has no comments, so we can just take the last word
        // TODO We also assume that only one declarator is present in the lhs
        // to be able to find the variable name and ignore statements before the variable name that might be in the same line (unlikely)
        var equalsLhsReverse = new StringBuilder(equalsLhs).reverse().toString();

        var varNameBuilder = new StringBuilder();

        for (var c : equalsLhsReverse.toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == '_') {
                varNameBuilder.append(c);
            } else {
                break;
            }
        }

        var varName = varNameBuilder.reverse().toString();

        System.out.println("Variable name: " + varName);

        var variable = mendingTable.variables().get(varName);

        // we dont need to set the symbols in EnumType and RecordType because they are already set in the QualType
        var qualTypeArgs = diag.message().args().stream().filter(
                arg -> arg.kind() == DiagnosticArgKind.QUALTYPE
        ).toList();

        assert qualTypeArgs.size() == 2;

        var correctQualTypeArg = (QualTypeArg) qualTypeArgs.get(1); // first is the wrong, second is the correct

        variable.setQualType(correctQualTypeArg.qualType());
        // TODO delete (should only be deleted if there are no usages of it)
    }

    public static void handleUnknown(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Unknown diagnostic ID '" + diag.id() + "' with message: " + diag.message().text());
    }
}
