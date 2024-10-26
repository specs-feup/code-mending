package pt.up.fe.specs.cmender.mending;

import pt.up.fe.specs.cmender.cli.CliReporting;
import pt.up.fe.specs.cmender.data.MendingDirData;
import pt.up.fe.specs.cmender.diag.Diagnostic;
import pt.up.fe.specs.cmender.diag.args.DeclContextArg;
import pt.up.fe.specs.cmender.diag.args.DeclarationNameArg;
import pt.up.fe.specs.cmender.diag.args.DiagnosticArgsMatcher;
import pt.up.fe.specs.cmender.diag.args.IdentifierArg;
import pt.up.fe.specs.cmender.diag.args.QualTypeArg;
import pt.up.fe.specs.cmender.diag.args.SIntArg;
import pt.up.fe.specs.cmender.diag.args.StdStringArg;
import pt.up.fe.specs.cmender.diag.args.UIntArg;
import pt.up.fe.specs.cmender.lang.declContext.RecordDecl;
import pt.up.fe.specs.cmender.lang.symbol.EnumSymbol;
import pt.up.fe.specs.cmender.lang.symbol.FunctionSymbol;
import pt.up.fe.specs.cmender.lang.symbol.RecordSymbol;
import pt.up.fe.specs.cmender.lang.symbol.TypedefSymbol;
import pt.up.fe.specs.cmender.lang.symbol.VariableSymbol;
import pt.up.fe.specs.cmender.lang.type.BuiltinType;
import pt.up.fe.specs.cmender.lang.type.EnumType;
import pt.up.fe.specs.cmender.lang.type.QualType;
import pt.up.fe.specs.cmender.lang.type.Qualifiers;
import pt.up.fe.specs.cmender.lang.type.RecordType;
import pt.up.fe.specs.cmender.lang.type.TypedefType;
import pt.up.fe.specs.cmender.logging.Logging;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

// TODO delete all types that arent being used (should only be deleted if there are no usages of it)

public class MendingHandlers {

    // When we don't know a symbol's type, we create a new starting type (mock type), which will be replaced later
    //    when we get more information about the symbol from the diagnostics
    // This mock type is a struct with no fields because it will be more likely to be replaced by a real type
    //    than a type like int or float, which get easily converted to other numeric types, changing some
    //    semantics of the original program
    // This struct will be typedef'd to a unique name for each symbol (i.e., for each variable, function, struct member)
    //      This way, we can easily replace the type of the symbol in the code by only looking at the diagnostic messages
    //       with type mismatch information


    // TODO find if we can have the handlers as objects (also they can be placed in the enum DiagnosticID)
    //  or in terms of performance it is better to have them as static methods
    public static void handleExtImplicitFunctionDeclC99(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("C99 implicit function declaration");

        String functionName;

        if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(IdentifierArg.class))) {
            functionName = ((IdentifierArg) diag.description().args().getFirst()).name();
        } else {
            CliReporting.error("Could not match diagnostic args for C99 implicit function declaration");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for C99 implicit function declaration");
            return;
        }

        var returnQualType = new QualType(
                "void",
                "void",
                "void diag_exporter_id",
                Qualifiers.unqualified(),
                new BuiltinType(BuiltinType.BuiltinKind.VOID, "void"),
                null);

        // typedef void cmender_type_0; is valid C code, so we can add the typedef of a void to the mending table
        var typedefType = new TypedefType(MendingTypeNameGenerator.newTypeName(), returnQualType);

        var typedefSymbol = new TypedefSymbol(typedefType.name(), typedefType);

        var function = new FunctionSymbol(functionName, typedefType);

        mendingTable.put(typedefSymbol);
        mendingTable.put(function);
        mendingTable.putTypeNameMapping(typedefType.name(), function);
    }

    public static void handleErrUndeclaredVarUse(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Undeclared variable use");
        // todo this type of diagnostic can also be raised when there is a type name from a typedef that is not defined

        /*var codeSnippet = diag.codeSnippet();

        var identifier = ((DeclarationNameArg) diag.message().args().getFirst()).name();

        var afterIdentifier = codeSnippet.substring(codeSnippet.indexOf(identifier) + identifier.length());

        for (var c : afterIdentifier.toCharArray()) {
            if (Character.isWhitespace(c)) {
                continue;
            }

            if (c == ',' || c == ';' || c == '=') {
                declareVar(diag, mendingTable);
            }

        }

        declareTypedef(diag, mendingTable);*/

        //var expectedTypes1 = List.of(DeclarationNameArg.class);
        if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(DeclarationNameArg.class))) {
            var varName = ((DeclarationNameArg) diag.description().args().getFirst()).name();

            declareVar(varName, mendingTable);
        } else {
            CliReporting.error("Could not match diagnostic args for undeclared variable use");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for undeclared variable use");
        }
    }

    public static void handleErrUndeclaredVarUseSuggest(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Undeclared variable use with suggestion");
        // todo this type of diagnostic can also be raised when there is a type name from a typedef that is not defined

        String varName;

        if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(DeclarationNameArg.class, StdStringArg.class))) {
            varName = ((DeclarationNameArg) diag.description().args().getFirst()).name();
        } else if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(IdentifierArg.class, StdStringArg.class))) {
            varName = ((IdentifierArg) diag.description().args().getFirst()).name();
        } else {
            CliReporting.error("Could not match diagnostic args for undeclared variable use with suggestion");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for undeclared variable use with suggestion");
            return;
        }

        declareVar(varName, mendingTable);

    }

    public static void handleErrTypecheckConvertIncompatible(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Incompatible type conversion");

        if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(
                QualTypeArg.class,
                QualTypeArg.class,
                SIntArg.class,
                UIntArg.class,
                SIntArg.class))) {
            CliReporting.error("Could not match diagnostic args for incompatible type conversion");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for incompatible type conversion");
            return;
        }

        // lhs is the type of the variable being initialized, rhs is the type of the expression
        var lhsQualType = ((QualTypeArg) diag.description().args().getFirst()).qualType();
        var rhsQualType = ((QualTypeArg) diag.description().args().get(1)).qualType();

        // We need to find which side is the one we need to change
        //   1) if one of the sides has a type we don't control, we change the other side
        //   2) if both sides have types we control, we change the lhs (TODO is there a situation this can go in loop?)
        //  TODO is there a situation where there is a side with types we can control but still havent declared?
        //     perhaps not because Clang might need to have all variables with types declared before the conversion

        QualType qualTypeToBeChanged;
        QualType qualTypeToBeKept;

        boolean lhsIsGenerated = MendingTypeNameGenerator.isGeneratedTypeName(lhsQualType.typeAsString());
        boolean rhsIsGenerated = MendingTypeNameGenerator.isGeneratedTypeName(rhsQualType.typeAsString());

        if (lhsIsGenerated && !rhsIsGenerated) {
            qualTypeToBeChanged = lhsQualType;
            qualTypeToBeKept = rhsQualType;
        } else if (!lhsIsGenerated && rhsIsGenerated) {
            qualTypeToBeChanged = rhsQualType;
            qualTypeToBeKept = lhsQualType;
        } else if (lhsIsGenerated && rhsIsGenerated) {
            qualTypeToBeChanged = lhsQualType;
            qualTypeToBeKept = rhsQualType;
        } else {
            CliReporting.error("Both sides of the incompatible type conversion have types we dont control");
            Logging.FILE_LOGGER.error("Both sides of the incompatible type conversion have types we dont control");
            return;
        }

        var typedefSymbol = mendingTable.typedefs().get(qualTypeToBeChanged.typeAsString());

        typedefSymbol.setAliasedType(qualTypeToBeKept);

        // TODO for now we assume the lhs is a variable, but it can also be e.g., a struct member
        // For now we assume that the file has no comments, so we can just take the last word
        // TODO We also assume that only one declarator is present in the lhs
        // to be able to find the variable name and ignore statements before the variable name that might be in the same line (unlikely)
    }

    public static void handleErrTypecheckInvalidOperands(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Invalid operands");

        if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(QualTypeArg.class, QualTypeArg.class))) {
            CliReporting.error("Could not match diagnostic args for invalid operands");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for invalid operands");
            return;
        }

        var lhsQualType = ((QualTypeArg) diag.description().args().getFirst()).qualType();
        var rhsQualType = ((QualTypeArg) diag.description().args().get(1)).qualType();

        QualType qualTypeToBeChanged;
        QualType qualTypeToBeKept;

        boolean lhsIsGenerated = MendingTypeNameGenerator.isGeneratedTypeName(lhsQualType.typeAsString());
        boolean rhsIsGenerated = MendingTypeNameGenerator.isGeneratedTypeName(rhsQualType.typeAsString());

        if (lhsIsGenerated && !rhsIsGenerated) {
            qualTypeToBeChanged = lhsQualType;
            qualTypeToBeKept = rhsQualType;
        } else if (!lhsIsGenerated && rhsIsGenerated) {
            qualTypeToBeChanged = rhsQualType;
            qualTypeToBeKept = lhsQualType;
        } else if (lhsIsGenerated && rhsIsGenerated) {
            // TODO for now assume that it's only number operands, and not _Bool operands

            if (!lhsQualType.type().isNumericType()) {
                var typedefSymbol = mendingTable.typedefs().get(lhsQualType.typeAsString());

                typedefSymbol.setAliasedType(new QualType(
                        "int",
                        "int",
                        "int diag_exporter_id",
                        Qualifiers.unqualified(),
                        new BuiltinType(BuiltinType.BuiltinKind.INT, "int"),
                        null));
            }

            if (!rhsQualType.type().isNumericType()) {
                var typedefSymbol = mendingTable.typedefs().get(rhsQualType.typeAsString());

                typedefSymbol.setAliasedType(new QualType(
                        "int",
                        "int",
                        "int diag_exporter_id",
                        Qualifiers.unqualified(),
                        new BuiltinType(BuiltinType.BuiltinKind.INT, "int"),
                        null));
            }

            return;
        } else {
            CliReporting.error("Both sides of the invalid operands have types we dont control");
            Logging.FILE_LOGGER.error("Both sides of the invalid operands have types we dont control");
            return;
        }

        var typedefSymbol = mendingTable.typedefs().get(qualTypeToBeChanged.typeAsString());

        typedefSymbol.setAliasedType(qualTypeToBeKept);
    }

    public static void handleErrPPFileNotFound(Diagnostic diag, MendingTable mendingTable, MendingDirData mendingDirData) {
       System.out.println("File not found");

       // INSIGHT: This diagnostic's level is fatal by default. clang will stop the compilation process if this diagnostic is raised
       var includePath = mendingDirData.includePath();

       try {

           if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(StdStringArg.class))) {
               CliReporting.error("Could not match diagnostic args for file not found");
               Logging.FILE_LOGGER.error("Could not match diagnostic args for file not found");
               return;
           }

           var stdStringArg = (StdStringArg) diag.description().args().getFirst();

           var headerFilePath = Paths.get(includePath, stdStringArg.string());

           Files.createDirectories(headerFilePath.getParent());

           Files.createFile(headerFilePath);
       } catch (Exception e) {
           CliReporting.error("Failed to handle file not found: " + e.getMessage());
           Logging.FILE_LOGGER.error("Failed to handle file not found: {}", e.getMessage());
       }

       // TODO we can try to find the file in the include paths and add it to the mending table

   }

    public static void handleErrTypecheckDeclIncompleteType(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Incomplete type declaration");

        // Most certainly this diagnostic only happens if the type is one whose name we dont control
        //  but have to declare it to be able to use it in the code
        //  This is because we declare and define all tag types in the mending table

        if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(QualTypeArg.class))) {
            CliReporting.error("Could not match diagnostic args for incomplete type declaration");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for incomplete type declaration");
            return;
        }

        var qualType = ((QualTypeArg) diag.description().args().getFirst()).qualType();

        if (qualType.type().isRecordType()) {
            var recordType = (RecordType) qualType.type();

            var recordSymbol = new RecordSymbol(recordType.name());

            mendingTable.put(recordSymbol);
        } else if (qualType.type().isEnumType()) {
            var enumType = (EnumType) qualType.type();

            var enumSymbol = new EnumSymbol(enumType.name());

            mendingTable.put(enumSymbol);
        } else {
            CliReporting.error("Incomplete type declaration for non-record and non-enum type");
            Logging.FILE_LOGGER.error("Incomplete type declaration for non-record and non-enum type");
        }
   }

    public static void handleErrNoMember(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("No member");

        if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(DeclarationNameArg.class, DeclContextArg.class))) {
            CliReporting.error("Could not match diagnostic args for no member diagnostic");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for no member diagnostic");
            return;
        }

        var memberName = ((DeclarationNameArg) diag.description().args().getFirst()).name();
        var declContext = ((DeclContextArg) diag.description().args().get(1)).declContext();

        switch (declContext.kind()) {
            case RECORD: {
                var recordDeclContext = (RecordDecl) declContext;

                if (recordDeclContext.tagKind() != RecordType.RecordKind.STRUCT) {
                    CliReporting.error("No member diagnostic for non-struct record");
                    Logging.FILE_LOGGER.error("No member diagnostic for non-struct record");
                    return;
                }

                var recordSymbol = mendingTable.structs().get(recordDeclContext.name());

                if (recordSymbol == null) {
                    CliReporting.error("No member diagnostic for undeclared struct");
                    Logging.FILE_LOGGER.error("No member diagnostic for undeclared struct");
                    return;
                }

                var structType = new RecordType(MendingTypeNameGenerator.newStructName(), RecordType.RecordKind.STRUCT);
                var structSymbol = new RecordSymbol(structType.name());

                var typedefType = new TypedefType(MendingTypeNameGenerator.newTypeName(), new QualType(
                        "struct " + structType.name(),
                        "struct " + structType.name(),
                        "struct " + structType.name() + " diag_exporter_id",
                        Qualifiers.unqualified(),
                        structType,
                        null));

                var typedefSymbol = new TypedefSymbol(typedefType.name(), typedefType);

                var member = new RecordSymbol.Member(memberName, typedefType);
                mendingTable.put(structSymbol);
                mendingTable.put(typedefSymbol);
                mendingTable.putTypeNameMapping(typedefType.name(), member);
                recordSymbol.addMember(member);
            }
                break;
            default: {
                return;
            }
        }
   }

    public static void handleUnknown(Diagnostic diag, MendingTable mendingTable) {
        CliReporting.error("Unknown diagnostic ID '" + diag.id() + "' with message: " + diag.description().message());
        Logging.FILE_LOGGER.error("Unknown diagnostic ID '" + diag.id() + "' with message: " + diag.description().message());
    }

    public static void declareVar(String varName, MendingTable mendingTable) {
        var structType = new RecordType(MendingTypeNameGenerator.newStructName(), RecordType.RecordKind.STRUCT);
        var structSymbol = new RecordSymbol(structType.name());

        var typedefType = new TypedefType(MendingTypeNameGenerator.newTypeName(), new QualType(
                "struct " + structType.name(),
                "struct " + structType.name(),
                "struct " + structType.name() + " diag_exporter_id",
                Qualifiers.unqualified(),
                structType,
                null));

        var typedefSymbol = new TypedefSymbol(typedefType.name(), typedefType);

        var variable = new VariableSymbol(varName, typedefType);

        mendingTable.put(structSymbol);
        mendingTable.put(typedefSymbol);
        mendingTable.put(variable);
        mendingTable.putTypeNameMapping(typedefType.name(), variable);
    }

    public static void declareTypedef(Diagnostic diag, MendingTable mendingTable) {
        var typedefName = ((DeclarationNameArg) diag.description().args().getFirst()).name();

        var structType = new RecordType(MendingTypeNameGenerator.newStructName(), RecordType.RecordKind.STRUCT);
        var structSymbol = new RecordSymbol(structType.name());

        var typedefType = new TypedefType(typedefName, new QualType(
                "struct " + structType.name(),
                "struct " + structType.name(),
                "struct " + structType.name() + " diag_exporter_id",
                Qualifiers.unqualified(),
                structType,
                null));

        var typedefSymbol = new TypedefSymbol(typedefType.name(), typedefType);

        mendingTable.put(structSymbol);
        mendingTable.put(typedefSymbol);
    }
}
