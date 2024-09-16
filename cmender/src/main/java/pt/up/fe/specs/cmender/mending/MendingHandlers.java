package pt.up.fe.specs.cmender.mending;

import pt.up.fe.specs.cmender.diag.Diagnostic;
import pt.up.fe.specs.cmender.diag.args.DeclarationNameArg;
import pt.up.fe.specs.cmender.diag.args.IdentifierArg;
import pt.up.fe.specs.cmender.lang.symbol.Function;
import pt.up.fe.specs.cmender.lang.symbol.Struct;
import pt.up.fe.specs.cmender.lang.symbol.Type;
import pt.up.fe.specs.cmender.lang.symbol.Typedef;
import pt.up.fe.specs.cmender.lang.symbol.Variable;

public class MendingHandlers {

    private static final String UNNAMED_SYMBOL_NAME_PREFIX = "cmender_unnamed_symbol_";

    private static int unnamedSymbolCounter = 0;

    private static String newName() {
        return UNNAMED_SYMBOL_NAME_PREFIX + unnamedSymbolCounter++;
    }

    private static Type createNewUnnamedStartingType(MendingTable mendingTable) {
        var struct = new Struct(newName());

        mendingTable.structs().put(struct.name(), struct);

        var typedef = new Typedef(newName(), struct);

        mendingTable.typedefs().put(typedef.name(), typedef);
        return typedef;
    }

    // TODO find if we can have the handlers as objects (also they can be placed in the enum DiagnosticID)
    //  or in terms of performance it is better to have them as static methods
    public static void handleExtImplicitFunctionDeclC99(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("C99 implicit function declaration");
        System.out.println(diag);

        var functionName = ((IdentifierArg) diag.message().args().getFirst()).name();

        var function = new Function(functionName);

        mendingTable.functions().put(functionName, function);
    }

    public static void handleErrUndeclaredVarUse(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Undeclared variable use");
        System.out.println(diag);

        var varName = ((DeclarationNameArg) diag.message().args().getFirst()).name();

        var variable = new Variable(varName, createNewUnnamedStartingType(mendingTable));

        mendingTable.variables().put(varName, variable);

    }

    public static void handleUnknown(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Unknown diagnostic ID " + diag.id() + " with message: " + diag.message().text());
    }
}
