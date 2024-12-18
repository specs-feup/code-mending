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
import pt.up.fe.specs.cmender.lang.Operations;
import pt.up.fe.specs.cmender.lang.declContext.RecordDecl;
import pt.up.fe.specs.cmender.lang.symbol.EnumSymbol;
import pt.up.fe.specs.cmender.lang.symbol.FunctionSymbol;
import pt.up.fe.specs.cmender.lang.symbol.RecordSymbol;
import pt.up.fe.specs.cmender.lang.symbol.TypedefSymbol;
import pt.up.fe.specs.cmender.lang.symbol.VariableSymbol;
import pt.up.fe.specs.cmender.lang.type.BuiltinType;
import pt.up.fe.specs.cmender.lang.type.EnumType;
import pt.up.fe.specs.cmender.lang.type.PointerType;
import pt.up.fe.specs.cmender.lang.type.QualType;
import pt.up.fe.specs.cmender.lang.type.Qualifiers;
import pt.up.fe.specs.cmender.lang.type.RecordType;
import pt.up.fe.specs.cmender.lang.type.TypeKind;
import pt.up.fe.specs.cmender.lang.type.TypeName;
import pt.up.fe.specs.cmender.lang.type.TypedefType;
import pt.up.fe.specs.cmender.logging.Logging;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

// TODO delete all types that arent being used (should only be deleted if there are no usages of it)

public interface MendingHandler {
    DiagnosticMendResult mend(List<Diagnostic> selectedDiags, MendingTable mendingTable, MendingDirData mendingDirData);

    // When we don't know a symbol's type, we create a new starting type (mock type), which will be replaced later
    //    when we get more information about the symbol from the diagnostics
    // This mock type is a struct with no fields because it will be more likely to be replaced by a real type
    //    than a type like int or float, which get easily converted to other numeric types, changing some
    //    semantics of the original program
    // This struct will be typedef'd to a unique name for each symbol (i.e., for each variable, function, struct member)
    //      This way, we can easily replace the type of the symbol in the code by only looking at the diagnostic messages
    //      with type mismatch information


    // INSIGHT 1: If any of the two sides of an assignment/initialization/return has a semantic error, Clang will not
    //      evaluate semantics regarding incompatible type conversions (and throw the diagnostic for it)
    // INSIGHT 2: On binary expressions (and sub-expressions), Clang will only evaluate type checking compatibility
    //      if all symbols used have been declared with types before.

    // TODO find if we can have the handlers as objects (also they can be placed in the enum DiagnosticID)
    //  or in terms of performance it is better to have them as static methods
    default void declareFunctionHeuristic(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("[declareFunctionHeuristic]");

        String functionName;

        if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(IdentifierArg.class))) { // ext_implicit_function_decl_c99
            functionName = ((IdentifierArg) diag.description().args().getFirst()).name();
        } else {
            CliReporting.error("Could not match diagnostic args for declareFunctionHeuristic");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for declareFunctionHeuristic");
            throw new RuntimeException("Could not match diagnostic args for declareFunctionHeuristic");
        }

        var returnQualType = new QualType(
                "void",
                "void",
                "void diag_exporter_id",
                Qualifiers.unqualified(),
                new BuiltinType(BuiltinType.BuiltinKind.VOID, "void"),
                null);

        // typedef void cmender_type_0; is valid C code, so we can add the typedef of a void to the mending table
        var typedefSymbol = createTypedef(returnQualType, new HashSet<>(List.of(
                TypeKind.BUILTIN,
                TypeKind.POINTER,
                TypeKind.ARRAY,
                TypeKind.RECORD,
                TypeKind.ENUM,
                TypeKind.TYPEDEF,
                TypeKind.FUNCTION
        )), mendingTable);
        var function = new FunctionSymbol(functionName, typedefSymbol.typedefType());

        mendingTable.put(function);
        mendingTable.putControlledTypedefAliasMapping(typedefSymbol.typedefType().name(), function);
    }

    default void declareVariableOrTypedefHeuristic(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("[declareVariableOrTypedefHeuristic]");
        // todo this type of diagnostic can also be raised when there is a type name from a typedef that is not defined

        String varName;
        if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(DeclarationNameArg.class))) { // err_undeclared_var_use
            varName = ((DeclarationNameArg) diag.description().args().getFirst()).name();
        } else if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(DeclarationNameArg.class, StdStringArg.class))) { // err_undeclared_var_use_suggest
            varName = ((DeclarationNameArg) diag.description().args().getFirst()).name();
        } else if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(IdentifierArg.class, StdStringArg.class))) { // err_undeclared_var_use_suggest
            varName = ((IdentifierArg) diag.description().args().getFirst()).name();
        } else {
            CliReporting.error("Could not match diagnostic args for declareVariableOrTypedefHeuristic");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for declareVariableOrTypedefHeuristic");
            throw new RuntimeException("Could not match diagnostic args for declareVariableOrTypedefHeuristic");
        }

        createMissingVariable(varName, mendingTable);
    }

    /*default void declareVariableHeuristic(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Undeclared variable use");

        if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(DeclarationNameArg.class))) {
            var varName = ((DeclarationNameArg) diag.description().args().getFirst()).name();

            createMissingVariable(varName, mendingTable);
        } else {
            CliReporting.error("Could not match diagnostic args for undeclared variable use");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for undeclared variable use");
            throw new RuntimeException("Could not match diagnostic args for undeclared variable use");
        }
    }

    default void declareVariableSuggestHeuristic(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Undeclared variable use with suggestion");

        String varName;

        if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(DeclarationNameArg.class, StdStringArg.class))) {
            varName = ((DeclarationNameArg) diag.description().args().getFirst()).name();
        } else if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(IdentifierArg.class, StdStringArg.class))) {
            varName = ((IdentifierArg) diag.description().args().getFirst()).name();
        } else {
            CliReporting.error("Could not match diagnostic args for undeclared variable use with suggestion");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for undeclared variable use with suggestion");
            throw new RuntimeException("Could not match diagnostic args for undeclared variable use with suggestion");
        }

        createMissingVariable(varName, mendingTable);
    }*/

    default void adjustConversionTypesHeuristic(Diagnostic diag, MendingTable mendingTable) {
        // TODO conversions of return types are not being handled (return type is source of truth)
        System.out.println("[adjustConversionTypesHeuristic]");
        // FIXME non generated typedef names are not being taken into account
        if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(
                QualTypeArg.class,
                QualTypeArg.class,
                SIntArg.class,
                UIntArg.class,
                SIntArg.class))) {
            CliReporting.error("Could not match diagnostic args for adjustConversionTypesHeuristic");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for adjustConversionTypesHeuristic");
            throw new RuntimeException("Could not match diagnostic args for adjustConversionTypesHeuristic");
        }

        // lhs is the type of the variable being initialized, rhs is the type of the expression
        var lhsQualType = ((QualTypeArg) diag.description().args().getFirst()).qualType();
        var rhsQualType = ((QualTypeArg) diag.description().args().get(1)).qualType();

        System.out.println("lhsType: " + lhsQualType.typeAsString() + " (" + lhsQualType.canonicalTypeAsString() + ")");
        System.out.println("rhsType: " + rhsQualType.typeAsString() + " (" + rhsQualType.canonicalTypeAsString() + ")");

        var lhsTypeName = lhsQualType.typeName();
        var rhsTypeName = rhsQualType.typeName();

        System.out.println("lhsTypeName: " + lhsTypeName);
        System.out.println("rhsTypeName: " + rhsTypeName);

        if (lhsTypeName.isEmpty() && rhsTypeName.isEmpty()) {
            CliReporting.error("Both sides are derived types without a name. Not yet implemented. This ");
            throw new RuntimeException("Both sides are derived types without a name. Not yet implemented. This ");
        }

        var lhsIsSourceOfTruth = lhsTypeName.isEmpty() ||
                mendingTable.isControlledAndNamedTagType(lhsTypeName.get()) || mendingTable.isUncontrolled(lhsTypeName.get());
        var rhsIsSourceOfTruth = rhsTypeName.isEmpty() ||
                mendingTable.isControlledAndNamedTagType(rhsTypeName.get()) || mendingTable.isUncontrolled(rhsTypeName.get());

        if (lhsIsSourceOfTruth && rhsIsSourceOfTruth) {
            CliReporting.error("Both sides are source of truth. Cant do anything");
            throw new RuntimeException("Both sides are source of truth. Cant do anything");
        } else if (lhsIsSourceOfTruth ^ rhsIsSourceOfTruth) {
            var sourceOfTruthQualType = lhsIsSourceOfTruth ? lhsQualType : rhsQualType;
            var toBeChangedQualType = lhsIsSourceOfTruth ? rhsQualType : lhsQualType;

            System.out.println("One side is source of truth: " + (lhsIsSourceOfTruth? "lhs" : "rhs"));

            // Because all types from missing declarations will be typedef'd, we can change the aliased type of the typedef
            assert (toBeChangedQualType.typeName().isPresent() && toBeChangedQualType.typeName().get().isTypedefAlias());
            System.out.println(toBeChangedQualType);

            var typedefSymbol = mendingTable.typedefs().get(toBeChangedQualType.typeName().get().typeName());

            typedefSymbol.setAliasedType(sourceOfTruthQualType);
            typedefSymbol.setPermittedTypes(Set.of());

        } else { // neither is source of truth. are also both controlled typedef aliases
            System.out.println("Both sides are controlled typedef aliases (neither is source of truth)");
            var lhsTypename = lhsTypeName.get();
            var rhsTypename = rhsTypeName.get();

            var lhsTypedefSymbol = mendingTable.typedefs().get(lhsTypename.typeName());
            var rhsTypedefSymbol = mendingTable.typedefs().get(rhsTypename.typeName());

            if (!lhsTypedefSymbol.canChangeAliasedType() && !rhsTypedefSymbol.canChangeAliasedType()) {
                // TODO might need to skip this diagnostic to the next one because we are changing the aliased type
                CliReporting.error("Both sides are controlled typedef aliases but none can change the aliased type");
                throw new RuntimeException("Both sides are controlled typedef aliases but none can change the aliased type");
            } else if (lhsTypedefSymbol.canChangeAliasedType() ^ rhsTypedefSymbol.canChangeAliasedType()) {
                var sourceOfTruthTypedefSymbol = lhsTypedefSymbol.canChangeAliasedType() ? rhsTypedefSymbol : lhsTypedefSymbol;
                var toBeChangedTypedefSymbol = lhsTypedefSymbol.canChangeAliasedType() ? lhsTypedefSymbol : rhsTypedefSymbol;

                System.out.println("One side can change aliased type: " + (lhsTypedefSymbol.canChangeAliasedType()? "lhs" : "rhs"));

                toBeChangedTypedefSymbol.setAliasedType(sourceOfTruthTypedefSymbol.typedefType().aliasedType());
                toBeChangedTypedefSymbol.setPermittedTypes(Set.of());
            } else { // both can change aliased type
                System.out.println("Both sides can change aliased type");
                var commonPermittedTypes = new HashSet<>(lhsTypedefSymbol.permittedTypes());
                commonPermittedTypes.retainAll(rhsTypedefSymbol.permittedTypes());

                if (commonPermittedTypes.isEmpty()) {
                    // TODO might need to skip this diagnostic to the next one because we are changing the aliased type
                    CliReporting.error("Both sides are controlled typedef aliases but none can change the aliased type to a common type");
                    throw new RuntimeException("Both sides are controlled typedef aliases but none can change the aliased type to a common type");
                }

                var sourceOfTruthTypedefSymbol = rhsTypedefSymbol;

                lhsTypedefSymbol.setAliasedType(sourceOfTruthTypedefSymbol.typedefType().aliasedType());
                lhsTypedefSymbol.setPermittedTypes(commonPermittedTypes);
            }
        }



        // We need to find which side is the one we need to change
        //   1) if one of the sides has a type we don't control, we change the other side (later is source of truth)
        //   2) if both sides have types we control, we change based on the permitted types of the typedef

        // The lhs can either be a math expression (in which case the rhs needs to be a number) or a single value
        //    in which case the rhs can be a number or a variable

        // INSIGHT Most likely there is not a single situation where there is a side with types we control but still
        //     havent declared. This is because Clang needs to have all variables with types declared before analysing
        //     the type conversion and throwing this diagnostic. This means we assume that we dont need to declare
        //     new types, just change the aliased type of the typedef


        // TODO multiple declarator
    }

    default void adjustOperandTypesHeuristic(Diagnostic diag, MendingTable mendingTable) {
        // TODO unary operations are not being handled
        // TODO in "==" operation, both operands can be pointers
        System.out.println("[adjustOperandTypesHeuristic]");

        if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(QualTypeArg.class, QualTypeArg.class))) {
            CliReporting.error("Could not match diagnostic args for invalid operands");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for adjustOperandTypesHeuristic");
            throw new RuntimeException("Could not match diagnostic args for adjustOperandTypesHeuristic");
        }

        var lhsQualType = ((QualTypeArg) diag.description().args().getFirst()).qualType();
        var rhsQualType = ((QualTypeArg) diag.description().args().get(1)).qualType();
        //var op = diag.location().presumedLoc().encompassingCode();
        var op = diag.location().isFileLoc()? diag.location().presumedLoc().encompassingCode() : diag.location().spellingLocs().getFirst().encompassingCode();

        System.out.println("lhsType: " + lhsQualType.typeAsString() + " (" + lhsQualType.canonicalTypeAsString() + ")");
        System.out.println("rhsType: " + rhsQualType.typeAsString() + " (" + rhsQualType.canonicalTypeAsString() + ")");
        System.out.println("op: " + op);

        var lhsTypeName = lhsQualType.typeName();
        var rhsTypeName = rhsQualType.typeName();

        System.out.println("lhsTypeName: " + lhsTypeName);
        System.out.println("rhsTypeName: " + rhsTypeName);

        if (lhsTypeName.isEmpty() && rhsTypeName.isEmpty()) {
            CliReporting.error("Both sides are derived types without a name. Not yet implemented. This ");
            throw new RuntimeException("Both sides are derived types without a name. Not yet implemented. This ");
        }

        var lhsIsSourceOfTruth = lhsTypeName.isEmpty() ||
                mendingTable.isControlledAndNamedTagType(lhsTypeName.get()) || mendingTable.isUncontrolled(lhsTypeName.get());
        var rhsIsSourceOfTruth = rhsTypeName.isEmpty() ||
                mendingTable.isControlledAndNamedTagType(rhsTypeName.get()) || mendingTable.isUncontrolled(rhsTypeName.get());

        if (lhsIsSourceOfTruth && rhsIsSourceOfTruth) {
            CliReporting.error("Both sides are source of truth. Cant do anything");
            throw new RuntimeException("Both sides are source of truth. Cant do anything");
        } else if (lhsIsSourceOfTruth ^ rhsIsSourceOfTruth) {
            System.out.println("One side is source of truth");

            handleInvalidOperandsWithOneSideSourceOfTruth(lhsQualType, rhsQualType, lhsIsSourceOfTruth, op, mendingTable);
        } else { // neither is source of truth. are also both controlled typedef aliases
            System.out.println("Both sides are controlled typedef aliases (neither is source of truth)");

            handleInvalidOperandsWithNoSideSourceOfTruth(lhsQualType, rhsQualType, lhsTypeName.get(), rhsTypeName.get(), op, mendingTable);
        }

        // TODO for now assume that it's only number operands, and not _Bool operands
    }

    private void handleInvalidOperandsWithOneSideSourceOfTruth(
            QualType lhsQualType,
            QualType rhsQualType,
            boolean lhsIsSourceOfTruth,
            String op,
            MendingTable mendingTable) {
        var sourceOfTruthQualType = lhsIsSourceOfTruth ? lhsQualType : rhsQualType;
        var toBeChangedQualType = lhsIsSourceOfTruth ? rhsQualType : lhsQualType;
        // Because all types from missing declarations will be typedef'd, we can change the aliased type of the typedef
        assert toBeChangedQualType.typeName().isPresent() && toBeChangedQualType.typeName().get().isTypedefAlias();


        var typedefSymbol = mendingTable.typedefs().get(toBeChangedQualType.typeName().get().typeName());

        if (sourceOfTruthQualType.type().isPointerType() || sourceOfTruthQualType.type().isArrayType()) {
            // cases where pointer arithmetic valid:
            //     ptr1 - ptr2;
            //     ptr - intVal
            //     ptr + intVal
            //     ptr - intVal
            //     ptr1 == ptr2 or any op
            //     ptr == intVal or any op

            if (Operations.isComparison(op)) {
                // pointers and arrays can be compared with pointers and arrays.
                // comparison can also be done with integral and enum types,
                //   this however is not a usual operation. as such we will create a pointer.

                typedefSymbol.setAliasedType(new QualType(
                        "int *",
                        "int *",
                        "int *diag_exporter_id",
                        Qualifiers.unqualified(),
                        new PointerType(new QualType(
                                "int",
                                "int",
                                "int diag_exporter_id",
                                Qualifiers.unqualified(),
                                new BuiltinType(BuiltinType.BuiltinKind.INT, "int"),
                                null)),
                        null));

                typedefSymbol.setPermittedTypes(Set.of(TypeKind.BUILTIN, TypeKind.ENUM)); // TODO maybe we need to restrict to integral and not builtin
            } else if (Operations.isBinarySum(op)) {
                // pointers and arrays can only be added with integral types and enums

                typedefSymbol.setAliasedType(new QualType(
                        "int",
                        "int",
                        "int diag_exporter_id",
                        Qualifiers.unqualified(),
                        new BuiltinType(BuiltinType.BuiltinKind.INT, "int"),
                        null));

                typedefSymbol.setPermittedTypes(Set.of(TypeKind.ENUM));
            } else if (Operations.isBinarySub(op) && !lhsIsSourceOfTruth) {
                // pointers and arrays can be subtracted from pointers and arrays.
                // integral types and enums can also be subtracted from pointers and arrays
                //   this however is not a usual operation. as such we will create a pointer.

                typedefSymbol.setAliasedType(new QualType(
                        "int *",
                        "int *",
                        "int *diag_exporter_id",
                        Qualifiers.unqualified(),
                        new PointerType(new QualType(
                                "int",
                                "int",
                                "int diag_exporter_id",
                                Qualifiers.unqualified(),
                                new BuiltinType(BuiltinType.BuiltinKind.INT, "int"),
                                null)),
                        null));

                typedefSymbol.setPermittedTypes(Set.of(TypeKind.BUILTIN, TypeKind.ENUM)); // TODO maybe we need to restrict to integral and not builtin
            } else {
                CliReporting.error("Pointer arithmetic operation not supported. Something went wrong");
                throw new RuntimeException("Pointer arithmetic operation not supported. Something went wrong");
            }

        } else if (sourceOfTruthQualType.type().isBuiltinType() || sourceOfTruthQualType.type().isEnumType()) {
            if (!typedefSymbol.canChangeAliasedType()) {
                // TODO will this ever happen? maybe only when there are no sources of truth
                // TODO might need to skip this diagnostic to the next one because we are changing the aliased type
                return;
            }

            if (typedefSymbol.typedefType().aliasedType().type().isRecordType()) {
                typedefSymbol.setAliasedType(new QualType(
                        "int",
                        "int",
                        "int diag_exporter_id",
                        Qualifiers.unqualified(),
                        new BuiltinType(BuiltinType.BuiltinKind.INT, "int"),
                        null));

                typedefSymbol.setPermittedTypes(Set.of(TypeKind.ENUM, TypeKind.POINTER, TypeKind.ARRAY));
            }

            // if non-source of truth is pointer or array, we dont do anything, wont even reach here (doesn't raise error)

        } else { // source of truth is not a pointer, array or number or enum (i.e., is a struct or union)
            CliReporting.error("Source of truth is not a pointer, array or number or enum and is used in an invalid operation");
            throw new RuntimeException("Source of truth is not a pointer, array or number or enum and is used in an invalid operation");
        }
    }

    private void handleInvalidOperandsWithNoSideSourceOfTruth(
            QualType lhsQualType,
            QualType rhsQualType,
            TypeName lhsTypeName,
            TypeName rhsTypeName,
            String op,
            MendingTable mendingTable) {
        var lhsTypedefSymbol = mendingTable.typedefs().get(lhsTypeName.typeName());
        var rhsTypedefSymbol = mendingTable.typedefs().get(rhsTypeName.typeName());

        if (!lhsTypedefSymbol.canChangeAliasedType() && !rhsTypedefSymbol.canChangeAliasedType()) {
            // TODO might need to skip this diagnostic to the next one because we are changing the aliased type
            CliReporting.error("Both sides are controlled typedef aliases but none can change the aliased type");
            throw new RuntimeException("Both sides are controlled typedef aliases but none can change the aliased type");
        } else if (lhsTypedefSymbol.canChangeAliasedType() ^ rhsTypedefSymbol.canChangeAliasedType()) {
            var sourceOfTruthTypedefSymbol = lhsTypedefSymbol.canChangeAliasedType() ? lhsTypedefSymbol : rhsTypedefSymbol;
            var toBeChangedTypedefSymbol = lhsTypedefSymbol.canChangeAliasedType() ? rhsTypedefSymbol : lhsTypedefSymbol;

            if (sourceOfTruthTypedefSymbol.typedefType().aliasedType().type().isPointerType() ||
                    sourceOfTruthTypedefSymbol.typedefType().aliasedType().type().isArrayType()) {
                // Because you can only add pointers and arrays to integral types
                toBeChangedTypedefSymbol.setAliasedType(new QualType(
                        "int",
                        "int",
                        "int diag_exporter_id",
                        Qualifiers.unqualified(),
                        new BuiltinType(BuiltinType.BuiltinKind.INT, "int"),
                        null));

                // Because you can only add pointers and arrays to integral types
                toBeChangedTypedefSymbol.setPermittedTypes(Set.of(TypeKind.ENUM));
            } else if (sourceOfTruthTypedefSymbol.typedefType().aliasedType().type().isBuiltinType() ||
                    sourceOfTruthTypedefSymbol.typedefType().aliasedType().type().isEnumType()) {
                if (!toBeChangedTypedefSymbol.canChangeAliasedType()) {
                    // TODO might need to skip this diagnostic to the next one because we are changing the aliased type
                    return;
                }

                if (toBeChangedTypedefSymbol.typedefType().aliasedType().type().isRecordType()) {
                    toBeChangedTypedefSymbol.setAliasedType(new QualType(
                            "int",
                            "int",
                            "int diag_exporter_id",
                            Qualifiers.unqualified(),
                            new BuiltinType(BuiltinType.BuiltinKind.INT, "int"),
                            null));

                    toBeChangedTypedefSymbol.setPermittedTypes(Set.of(TypeKind.ENUM, TypeKind.POINTER, TypeKind.ARRAY));
                }

                // if is pointer or array, we dont do anything
            } else { // source of truth is not a pointer, array or number or enum (i.e., is a struct or union)
                CliReporting.error("Source of truth is not a pointer, array or number or enum and is used in an invalid operation (binary operation)");
                throw new RuntimeException("Source of truth is not a pointer, array or number or enum and is used in an invalid operation (binary operation)");
            }
        } else { // both can change aliased type

            // Because you can only add pointers and arrays to integral types and enums
            lhsTypedefSymbol.permittedTypes().remove(TypeKind.RECORD);
            rhsTypedefSymbol.permittedTypes().remove(TypeKind.RECORD);

            var lhsIsRecord = lhsTypedefSymbol.typedefType().aliasedType().type().isRecordType();
            var rhsIsRecord = rhsTypedefSymbol.typedefType().aliasedType().type().isRecordType();

            var lhsHasPermittedPointerOrArray = lhsTypedefSymbol.permittedTypes().contains(TypeKind.POINTER) ||
                    lhsTypedefSymbol.permittedTypes().contains(TypeKind.ARRAY);

            var rhsHasPermittedPointerOrArray = rhsTypedefSymbol.permittedTypes().contains(TypeKind.POINTER) ||
                    rhsTypedefSymbol.permittedTypes().contains(TypeKind.ARRAY);

            var lhsHasPermittedBuiltInOrEnum = lhsTypedefSymbol.permittedTypes().contains(TypeKind.BUILTIN) ||
                    lhsTypedefSymbol.permittedTypes().contains(TypeKind.ENUM);

            var rhsHasPermittedBuiltInOrEnum = rhsTypedefSymbol.permittedTypes().contains(TypeKind.BUILTIN) ||
                    rhsTypedefSymbol.permittedTypes().contains(TypeKind.ENUM);

            if (lhsIsRecord) {
                lhsTypedefSymbol.setAliasedType(new QualType(
                        "int",
                        "int",
                        "int diag_exporter_id",
                        Qualifiers.unqualified(),
                        new BuiltinType(BuiltinType.BuiltinKind.INT, "int"),
                        null));

                lhsTypedefSymbol.setPermittedTypes(Set.of(TypeKind.ENUM, TypeKind.POINTER, TypeKind.ARRAY));
            }

            if (rhsIsRecord) {
                rhsTypedefSymbol.setAliasedType(new QualType(
                        "int",
                        "int",
                        "int diag_exporter_id",
                        Qualifiers.unqualified(),
                        new BuiltinType(BuiltinType.BuiltinKind.INT, "int"),
                        null));

                rhsTypedefSymbol.setPermittedTypes(Set.of(TypeKind.ENUM, TypeKind.POINTER, TypeKind.ARRAY));
            }


                /*if (lhsHasPointerOrArray && rhsHasBuiltInOrEnum) {

                }*/

            var commonPermittedTypes = new HashSet<>(lhsTypedefSymbol.permittedTypes());
            commonPermittedTypes.retainAll(rhsTypedefSymbol.permittedTypes());


            if (commonPermittedTypes.isEmpty()) {
                // TODO might need to skip this diagnostic to the next one because we are changing the aliased type
                CliReporting.error("Both sides are controlled typedef aliases but none can change the aliased type to a common type");
                throw new RuntimeException("Both sides are controlled typedef aliases but none can change the aliased type to a common type");
            }


        }
    }


    default void createHeaderFileHeuristic(Diagnostic diag, MendingTable mendingTable, MendingDirData mendingDirData) {
        System.out.println("[createHeaderFileHeuristic]");

        // INSIGHT: This diagnostic's level is fatal by default. clang will stop the compilation process if this diagnostic is raised
        var includePath = mendingDirData.includePath();

        try {

            if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(StdStringArg.class))) {
                CliReporting.error("Could not match diagnostic args for createHeaderFileHeuristic");
                Logging.FILE_LOGGER.error("Could not match diagnostic args for createHeaderFileHeuristic");
                throw new RuntimeException("Could not match diagnostic args for createHeaderFileHeuristic");
            }

            var stdStringArg = (StdStringArg) diag.description().args().getFirst();

            var headerFilePath = Paths.get(includePath, stdStringArg.string());

            Files.createDirectories(headerFilePath.getParent());

            Files.createFile(headerFilePath);
        } catch (Exception e) {
            CliReporting.error("Failed to handle missing pp file: " + e.getMessage());
            Logging.FILE_LOGGER.error("Failed to handle missing pp file: {}", e.getMessage());
            throw new RuntimeException("Failed to handle missing pp file: " + e.getMessage());
        }

        // TODO we can try to find the file in the include paths and add it to the mending table

    }

    default void defineTagTypeHeuristic(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("[defineTagTypeHeuristic]");

        // Most certainly this diagnostic only happens if the type is one whose name we dont control
        //  but have to declare it to be able to use it in the code
        //  This is because we declare and define all tag types in the mending table

        if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(QualTypeArg.class))) {
            CliReporting.error("Could not match diagnostic args for incomplete type declaration");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for incomplete type declaration");
            throw new RuntimeException("Could not match diagnostic args for incomplete type declaration");
        }

        var qualType = ((QualTypeArg) diag.description().args().getFirst()).qualType();

        if (qualType.type().isRecordType()) {
            var recordType = (RecordType) qualType.type();

            var recordSymbol = new RecordSymbol(recordType.name());

            mendingTable.put(recordSymbol);
        } else if (qualType.type().isEnumType()) {
            var enumType = (EnumType) qualType.type();

            var enumSymbol = new EnumSymbol(enumType.name(),
                    List.of(MendingTypeNameGenerator.newEnumConstantName())); // we need to add a starting enum constant because it's mandatory for the definition

            mendingTable.put(enumSymbol);
        } else {
            CliReporting.error("Incomplete type declaration for non-record and non-enum type");
            Logging.FILE_LOGGER.error("Incomplete type declaration for non-record and non-enum type");
            throw new RuntimeException("Incomplete type declaration for non-record and non-enum type");
        }
    }

    default void addRecordMemberHeuristic(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("[addRecordMemberHeuristic]");

        if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(DeclarationNameArg.class, DeclContextArg.class))) {
            CliReporting.error("Could not match diagnostic args for addRecordMemberHeuristic");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for addRecordMemberHeuristic");
            throw new RuntimeException("Could not match diagnostic args for addRecordMemberHeuristic");
        }

        var memberName = ((DeclarationNameArg) diag.description().args().getFirst()).name();
        var declContext = ((DeclContextArg) diag.description().args().get(1)).declContext();

        switch (declContext.kind()) {
            case RECORD: {
                var recordDeclContext = (RecordDecl) declContext;

                if (recordDeclContext.tagKind() != RecordType.RecordKind.STRUCT &&
                        recordDeclContext.tagKind() != RecordType.RecordKind.UNION) {
                    CliReporting.error("No member diagnostic for non-struct record");
                    Logging.FILE_LOGGER.error("No member diagnostic for non-struct record");
                    throw new RuntimeException("No member diagnostic for non-struct record");
                }

                var recordSymbol = mendingTable.records().get(recordDeclContext.name());

                if (recordSymbol == null) {
                    CliReporting.error("No member diagnostic for undeclared struct");
                    Logging.FILE_LOGGER.error("No member diagnostic for undeclared struct");
                    throw new RuntimeException("No member diagnostic for undeclared struct");
                }

                var memberTypedefSymbol = createTypedefOfGeneratedStructType(Set.of(TypeKind.BUILTIN, TypeKind.POINTER, TypeKind.ARRAY,
                        TypeKind.RECORD, TypeKind.ENUM, TypeKind.TYPEDEF, TypeKind.FUNCTION), mendingTable);

                var member = new RecordSymbol.Member(memberName, memberTypedefSymbol.typedefType());
                recordSymbol.addMember(member);
                mendingTable.putControlledTypedefAliasMapping(memberTypedefSymbol.typedefType().name(), member);
            }
            break;
            default: {
                return;
            }
        }
    }

    default void declareTypedefTypeAliasHeuristic(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("[declareTypedefTypeAliasHeuristic]");

        String typedefName;
        if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(IdentifierArg.class))) { // err_unknown_typename
            typedefName = ((IdentifierArg) diag.description().args().getFirst()).name();
        } else if (DiagnosticArgsMatcher.match(diag.description().args(), List.of(IdentifierArg.class))) { // err_unknown_typename_suggest
            typedefName = ((IdentifierArg) diag.description().args().getFirst()).name();
        } else {
            CliReporting.error("Could not match diagnostic args for declareTypedefTypeAliasHeuristic");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for declareTypedefTypeAliasHeuristic");
            throw new RuntimeException("Could not match diagnostic args for declareTypedefTypeAliasHeuristic");
        }

        createTypedefOfGeneratedStructType(typedefName, Set.of(TypeKind.BUILTIN, TypeKind.POINTER, TypeKind.ARRAY,
                TypeKind.RECORD, TypeKind.ENUM, TypeKind.TYPEDEF, TypeKind.FUNCTION), mendingTable);
    }

    /*default void createTypedefTypeAliasHeuristic(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Unknown typename");

        if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(IdentifierArg.class))) {
            CliReporting.error("Could not match diagnostic args for unknown typename");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for unknown typename");
            throw new RuntimeException("Could not match diagnostic args for unknown typename");
        }

        var typedefName = ((IdentifierArg) diag.description().args().getFirst()).name();

        createTypedefOfGeneratedStructType(typedefName, Set.of(TypeKind.BUILTIN, TypeKind.POINTER, TypeKind.ARRAY,
                TypeKind.RECORD, TypeKind.ENUM, TypeKind.TYPEDEF, TypeKind.FUNCTION), mendingTable);
    }

    default void createTypedefTypeAliasSuggestHeuristic(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("Unknown typename with suggestion");

        if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of(IdentifierArg.class, StdStringArg.class))) {
            CliReporting.error("Could not match diagnostic args for unknown typename");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for unknown typename");
            throw new RuntimeException("Could not match diagnostic args for unknown typename");
        }

        var typedefName = ((IdentifierArg) diag.description().args().getFirst()).name();

        createTypedefOfGeneratedStructType(typedefName, Set.of(TypeKind.BUILTIN, TypeKind.POINTER, TypeKind.ARRAY,
                TypeKind.RECORD, TypeKind.ENUM, TypeKind.TYPEDEF, TypeKind.FUNCTION), mendingTable);
    }*/

    default void adjustMemberReferenceHeuristic(Diagnostic dig, MendingTable mendingTable) {
        System.out.println("[adjustMemberReferenceHeuristic]");

        if (!DiagnosticArgsMatcher.match(dig.description().args(), List.of(QualTypeArg.class, SIntArg.class))) {
            CliReporting.error("Could not match diagnostic args for adjustMemberReferenceHeuristic");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for adjustMemberReferenceHeuristic");
            throw new RuntimeException("Could not match diagnostic args for adjustMemberReferenceHeuristic");
        }

        var qualType = ((QualTypeArg) dig.description().args().getFirst()).qualType();
        var shouldBePointer = ((SIntArg) dig.description().args().get(1)).integer() == 1;

        System.out.println("Type: " + qualType.typeAsString() + " (" + qualType.canonicalTypeAsString() + ")");
        var typeName = qualType.typeName();
        System.out.println("TypeName: " + typeName);
        if (shouldBePointer) {
            System.out.println("Should be pointer");
            var typedefSymbol = mendingTable.typedefs().get(qualType.typeAsString());

            typedefSymbol.setAliasedType(new QualType(
                    typedefSymbol.typedefType().name(),
                    qualType.canonicalTypeAsString() + " *",
                    qualType.canonicalTypeAsString() + " *diag_exporter_id",
                    Qualifiers.unqualified(),
                    new PointerType(typedefSymbol.typedefType().aliasedType()),
                    null));

            typedefSymbol.setPermittedTypes(Set.of());
        }
    }

    default void adjustSubscriptBaseHeuristic(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("[adjustSubscriptBaseHeuristic]");

        var codeSnippet = diag.codeSnippet();

        var baseEncompassingCode = diag.sourceRanges().getFirst().encompassingCode();
        var subscriptEncompassingCode = diag.sourceRanges().get(1).encompassingCode();

        var baseAndSubscriptEncompassingCode = baseEncompassingCode + "[" + subscriptEncompassingCode + "]";
        System.out.println("Base and subscript encompassing code: " + baseAndSubscriptEncompassingCode);
        // var subscriptLoc = diag.location().isFileLoc()? diag.location().presumedLoc() : diag.location().expansionLoc();

        // TODO for now we assume the entire subexpression is in the same line

        var symbol = mendingTable.arraySubscriptToCorrespondingSymbol().get(baseEncompassingCode);

        // TODO the level of spaghetti is incredible. probably needs AST to be able to handle this properly
        // TODO Here we are assuming that we will only get generated structs as the type of the subscript base. will probably backfire

        if (symbol != null) {
            System.out.println("Found array subscript mapping for base: " + baseEncompassingCode);

            if (symbol instanceof VariableSymbol variableSymbol) {
                variableSymbol.type().setAliasedType(new QualType(
                        variableSymbol.type().aliasedType().typeAsString() + " *",
                        variableSymbol.type().aliasedType().canonicalTypeAsString() + " *",
                        variableSymbol.type().aliasedType().canonicalTypeAsString() + " * diag_exporter_id",
                        Qualifiers.unqualified(),
                        new PointerType(variableSymbol.type().aliasedType()),
                        null));
                mendingTable.putControlledArraySubscriptMapping(baseAndSubscriptEncompassingCode, variableSymbol);
            } else if (symbol instanceof FunctionSymbol functionSymbol) {
                functionSymbol.returnType().setAliasedType(
                        new QualType(
                                functionSymbol.returnType().aliasedType().typeAsString() + " *",
                                functionSymbol.returnType().aliasedType().canonicalTypeAsString() + " *",
                                functionSymbol.returnType().aliasedType().canonicalTypeAsString() + " * diag_exporter_id",
                                Qualifiers.unqualified(),
                                new PointerType(functionSymbol.returnType().aliasedType()),
                                null));
                mendingTable.putControlledArraySubscriptMapping(baseAndSubscriptEncompassingCode, functionSymbol);
            } else if (symbol instanceof RecordSymbol.Member memberSymbol) {
                memberSymbol.type().setAliasedType(new QualType(
                        memberSymbol.type().aliasedType().typeAsString() + " *",
                        memberSymbol.type().aliasedType().canonicalTypeAsString() + " *",
                        memberSymbol.type().aliasedType().canonicalTypeAsString() + " * diag_exporter_id",
                        Qualifiers.unqualified(),
                        new PointerType(memberSymbol.type().aliasedType()),
                        null));
                mendingTable.putControlledArraySubscriptMapping(baseAndSubscriptEncompassingCode, memberSymbol);
            } else {
                CliReporting.error("Unknown symbol type for subscript value");
                Logging.FILE_LOGGER.error("Unknown symbol type for subscript value");
                throw new RuntimeException("Unknown symbol type for subscript value");
            }
        } else {
            System.out.println("Did not find array subscript mapping for base: " + baseEncompassingCode);
            var identifierPattern = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)$"); // TODO we will probably need to parse the baseEncompassingCode to get the identifier

            var matcher = identifierPattern.matcher(baseEncompassingCode);

            if (matcher.find()) {
                var varName = matcher.group(1);

                System.out.println(varName);

                var varSymbol = mendingTable.variables().get(varName);

                // TODO this is adding needless space between consecutive pointers

                /*
                varSymbol.type().setAliasedType(new QualType(
                        varSymbol.type().aliasedType().typeAsString() + " *",
                        varSymbol.type().aliasedType().canonicalTypeAsString() + " *",
                        varSymbol.type().aliasedType().canonicalTypeAsString() + " * diag_exporter_id",
                        Qualifiers.unqualified(),
                        new PointerType(varSymbol.type().aliasedType()),
                        null));
                        */

                var structType = varSymbol.type().aliasedType();

                var structTypedefAliasSymbol = createTypedef(structType, Set.of(), mendingTable); // change set.of()

                varSymbol.type().setAliasedType(
                        new QualType(
                                structTypedefAliasSymbol.typedefType().name() + " *",
                                structTypedefAliasSymbol.typedefType().name() + " *",
                                structTypedefAliasSymbol.typedefType().name() + " *diag_exporter_id",
                                Qualifiers.unqualified(),
                                new PointerType(new QualType(
                                        structTypedefAliasSymbol.typedefType().name(),
                                        structTypedefAliasSymbol.typedefType().aliasedType().canonicalTypeAsString(),
                                        structTypedefAliasSymbol.typedefType().aliasedType().canonicalTypeAsString() + " diag_exporter_id",
                                        Qualifiers.unqualified(),
                                        structTypedefAliasSymbol.typedefType().aliasedType().type(),
                                        null)),
                                null)
                );
                /*var newTypedefAliasName = MendingTypeNameGenerator.newTypedefAliasName();

                var newTypedefType = new TypedefType(newTypedefAliasName, new QualType(
                        varSymbol.type().aliasedType().typeAsString() + " *",
                        varSymbol.type().aliasedType().canonicalTypeAsString() + " *",
                        varSymbol.type().aliasedType().canonicalTypeAsString() + " * diag_exporter_id",
                        Qualifiers.unqualified(),
                        new PointerType(varSymbol.type().aliasedType()),
                        null));

                var newTypedefSymbol = new TypedefSymbol(newTypedefAliasName, newTypedefType, Set.of());

                varSymbol.type().setAliasedType(new QualType(
                        newTypedefAliasName,
                        newTypedefAliasName,
                        newTypedefAliasName + " diag_exporter_id",
                        Qualifiers.unqualified(),
                        newTypedefType,
                        null
                ));

                mendingTable.put(newTypedefSymbol);
                mendingTable.putControlledTypedefAliasMapping(newTypedefAliasName, varSymbol);*/
                mendingTable.putControlledArraySubscriptMapping(baseAndSubscriptEncompassingCode, varSymbol);

            } else {
                CliReporting.error("Could not find variable name in subscript value");
                Logging.FILE_LOGGER.error("Could not find variable name in subscript value");
                throw new RuntimeException("Could not find variable name in subscript value");
            }
        }
    }

    default void convertSubscriptToIntegerHeuristic(Diagnostic diag, MendingTable mendingTable) {
        System.out.println("[convertSubscriptToIntegerHeuristic]");

        if (!DiagnosticArgsMatcher.match(diag.description().args(), List.of())) {
            CliReporting.error("Could not match diagnostic args for subscript not integer");
            Logging.FILE_LOGGER.error("Could not match diagnostic args for subscript not integer");
            throw new RuntimeException("Could not match diagnostic args for subscript not integer");
        }

        var identifier = diag.sourceRanges().getFirst().encompassingCode();

        // TODO this works for when the array is in local scope (or similar). maybe we might need to put this const or even use a macro for compile time size
         // Macro might be the best option because
        mendingTable.variables().get(identifier).setType(
                new QualType(
                        "unsigned int",
                        "unsigned int",
                        "unsigned int diag_exporter_id",
                        Qualifiers.unqualified(),
                        new BuiltinType(BuiltinType.BuiltinKind.UINT, "unsigned int"),
                        null));

    }

    default void handleUnknown(Diagnostic diag, MendingTable mendingTable) {
        CliReporting.error("Unknown diagnostic ID '" + diag.labelId().toUpperCase() + "(" + diag.id() + ")' with message: " + diag.description().message());
        Logging.FILE_LOGGER.error("Unknown diagnostic ID '{}({})' with message: {}", diag.labelId().toUpperCase(), diag.id(), diag.description().message());
    }

    default void createMissingVariable(String varName, MendingTable mendingTable) {
        var typedefSymbol = createTypedefOfGeneratedStructType(
                Set.of(TypeKind.BUILTIN, TypeKind.POINTER, TypeKind.ARRAY, TypeKind.RECORD,
                        TypeKind.ENUM, TypeKind.TYPEDEF, TypeKind.FUNCTION), mendingTable);

        var variable = new VariableSymbol(varName, typedefSymbol.typedefType());

        mendingTable.put(variable);
        mendingTable.putControlledTypedefAliasMapping(typedefSymbol.typedefType().name(), variable);
    }

    // Used for when a symbol (i.e., variable, function, and struct member) is DECLARED in the code, but
    //    their types are typedef names that are not defined in the code but are being used
    // They ARE NOT generated typedef names, but struct types are created for them
    static TypedefSymbol createTypedefOfGeneratedStructType(
            String typedefName, Set<TypeKind> permittedTypes, MendingTable mendingTable) {

        var structType = new RecordType(MendingTypeNameGenerator.newTagTypeName(), RecordType.RecordKind.STRUCT);
        var structSymbol = new RecordSymbol(structType.name());

        mendingTable.put(structSymbol);

        return createTypedef(typedefName, new QualType(
                "struct " + structType.name(),
                "struct " + structType.name(),
                "struct " + structType.name() + " diag_exporter_id",
                Qualifiers.unqualified(),
                structType,
                null), permittedTypes, mendingTable);
    }

    // Used for when a symbol (i.e., variable, function, and struct member) is NOT DECLARED in the code,
    //    but is being used, and we need to create a new (starting/mock) type for it because we don't
    //    know its type yet.
    // They ARE generated typedef names, and struct types are created for them
    static TypedefSymbol createTypedefOfGeneratedStructType(
            Set<TypeKind> permittedTypes, MendingTable mendingTable) {
        return createTypedefOfGeneratedStructType(MendingTypeNameGenerator.newTypedefAliasName(), permittedTypes, mendingTable);
    }

    static TypedefSymbol createTypedef(String typedefName, QualType qualType, Set<TypeKind> permittedTypes, MendingTable mendingTable) {
        var typedefType = new TypedefType(typedefName, qualType);

        var typedefSymbol = new TypedefSymbol(typedefType.name(), typedefType, permittedTypes);

        mendingTable.put(typedefSymbol);

        return typedefSymbol;
    }

    static TypedefSymbol createTypedef(QualType qualType, Set<TypeKind> permittedTypes, MendingTable mendingTable) {
        return createTypedef(MendingTypeNameGenerator.newTypedefAliasName(), qualType, permittedTypes, mendingTable);
    }
}
