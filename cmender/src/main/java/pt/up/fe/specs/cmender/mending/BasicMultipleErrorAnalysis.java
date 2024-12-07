package pt.up.fe.specs.cmender.mending;

import pt.up.fe.specs.cmender.diag.DiagExporterSourceResult;
import pt.up.fe.specs.cmender.diag.Diagnostic;
import pt.up.fe.specs.cmender.diag.DiagnosticID;
import pt.up.fe.specs.cmender.diag.args.DeclarationNameArg;
import pt.up.fe.specs.cmender.diag.args.DiagnosticArgKind;
import pt.up.fe.specs.cmender.diag.args.IdentifierArg;
import pt.up.fe.specs.cmender.diag.args.QualTypeArg;
import pt.up.fe.specs.cmender.lang.type.EnumType;
import pt.up.fe.specs.cmender.lang.type.RecordType;

import java.util.List;
import java.util.Set;

public class BasicMultipleErrorAnalysis implements DiagnosticAnalysis {
    private final BasicFirstErrorAnalysis basicFirstErrorAnalysis;

    public BasicMultipleErrorAnalysis() {
        basicFirstErrorAnalysis = new BasicFirstErrorAnalysis();
    }

    // This will remove further occurences, meaning the one that remains will be the first one that appears
    //  TODO this will probably change line progression a lot
    private void removeExactlyRepeatedDiagnostics(List<Integer> diagnostics, DiagExporterSourceResult sourceResult) {
        for (int i = 0; i < diagnostics.size() - 1; i++) {
            var diag = sourceResult.diags().get(diagnostics.get(i));

            for (int j = i + 1; j < diagnostics.size(); j++) {
                var otherDiag = sourceResult.diags().get(diagnostics.get(j));
                if (diag.description().message().equals(otherDiag.description().message())) {
                    diagnostics.remove(j);
                    j--;
                }
            }
        }
    }

    private String getIdentifier(Diagnostic diag) {
        if (diag.id() == DiagnosticID.ERR_UNDECLARED_VAR_USE.getId()) {
            return ((DeclarationNameArg) diag.description().args().getFirst()).name();
        } else if (diag.id() == DiagnosticID.ERR_UNDECLARED_VAR_USE_SUGGEST.getId()) {
            if (diag.description().args().getFirst().kind() == DiagnosticArgKind.DECLARATION_NAME) {
                return ((DeclarationNameArg) diag.description().args().getFirst()).name();
            } else {
                return ((IdentifierArg) diag.description().args().getFirst()).name();
            }
        } else if (diag.id() == DiagnosticID.ERR_FUNC_DEF_INCOMPLETE_RESULT.getId() ||
                diag.id() == DiagnosticID.ERR_TYPECHECK_DECL_INCOMPLETE_TYPE.getId()) {

            var qualType = ((QualTypeArg) diag.description().args().getFirst()).qualType();

            if (qualType.type().isRecordType()) {
                var recordType = (RecordType) qualType.type();

                return recordType.name();

            } else if (qualType.type().isEnumType()) {
                var enumType = (EnumType) qualType.type();
                return enumType.name();
            }
        }
        else {
            return ((IdentifierArg) diag.description().args().getFirst()).name();
        }

        return "";
    }

    // ERR_UNDECLARED_VAR_USE and ERR_UNDECLARED_VAR_USE_SUGGEST are misleading because they can be used for typedefs
    //     as well, so we need to remove the ones that are the same as ERR_UNKNOWN_TYPENAME and ERR_UNKNOWN_TYPENAME_SUGGEST,
    //     the latter ones are more specific and should be kept because this way we dont need to check if the identifier
    //     is a typedef or a variable
    private void removeStdIdentifierSpaceSimilarDiagnostics(List<Integer> diagnostics, DiagExporterSourceResult sourceResult) {
        for (int i = 0; i < diagnostics.size() - 1; i++) {
            var diag = sourceResult.diags().get(diagnostics.get(i));

            if (diag.id() != DiagnosticID.ERR_UNDECLARED_VAR_USE.getId() &&
                    diag.id() != DiagnosticID.ERR_UNDECLARED_VAR_USE_SUGGEST.getId() &&
                    diag.id() != DiagnosticID.ERR_UNKNOWN_TYPENAME.getId() &&
                    diag.id() != DiagnosticID.ERR_UNKNOWN_TYPENAME_SUGGEST.getId()) {
                continue;
            }

            for (int j = 0; j < diagnostics.size(); j++) {
                if (i == j) {
                    continue;
                }

                var otherDiag = sourceResult.diags().get(diagnostics.get(j));


                if (otherDiag.id() != DiagnosticID.ERR_UNDECLARED_VAR_USE.getId() &&
                        otherDiag.id() != DiagnosticID.ERR_UNDECLARED_VAR_USE_SUGGEST.getId() &&
                        otherDiag.id() != DiagnosticID.ERR_UNKNOWN_TYPENAME.getId() &&
                        otherDiag.id() != DiagnosticID.ERR_UNKNOWN_TYPENAME_SUGGEST.getId()) {
                    continue;
                }

                // If the diagnostic is a undeclared variable and the other is a unknown typename, we can remove it
                //    because the unknown typename is more specific
                if ((diag.id() == DiagnosticID.ERR_UNDECLARED_VAR_USE.getId() ||
                        diag.id() == DiagnosticID.ERR_UNDECLARED_VAR_USE_SUGGEST.getId()) &&
                        (otherDiag.id() == DiagnosticID.ERR_UNKNOWN_TYPENAME.getId() ||
                                otherDiag.id() == DiagnosticID.ERR_UNKNOWN_TYPENAME_SUGGEST.getId())) {
                    continue;
                }

                var identifier = getIdentifier(diag);
                var otherIdentifier = getIdentifier(otherDiag);

                if (identifier.equals(otherIdentifier)) {
                    diagnostics.remove(j);
                    j--;
                }
            }
        }
        /*for (int i = 0; i < diagnostics.size() - 1; i++) {
            var diag = sourceResult.diags().get(diagnostics.get(i));

            if (diag.id() != DiagnosticID.ERR_UNDECLARED_VAR_USE.getId() &&
                    diag.id() != DiagnosticID.ERR_UNDECLARED_VAR_USE_SUGGEST.getId()) {
                continue;
            }

            var identifier = getIdentifier(diag);


            for (int j = i + 1; j < diagnostics.size(); j++) {
                var otherDiag = sourceResult.diags().get(diagnostics.get(j));

                if (otherDiag.id() != DiagnosticID.ERR_UNDECLARED_VAR_USE.getId() &&
                        otherDiag.id() != DiagnosticID.ERR_UNDECLARED_VAR_USE_SUGGEST.getId()) {
                    continue;
                }

                if (getIdentifier(otherDiag).equals(identifier)) {
                    diagnostics.remove(j);
                    j--;
                }


            }
        }*/
    }

    private void removeTagSpaceSimilarDiagnostics(List<Integer> diagnostics, DiagExporterSourceResult sourceResult) {
        for (int i = 0; i < diagnostics.size() - 1; i++) {
            var diag = sourceResult.diags().get(diagnostics.get(i));

            if (diag.id() != DiagnosticID.ERR_TYPECHECK_DECL_INCOMPLETE_TYPE.getId() &&
                    diag.id() != DiagnosticID.ERR_FUNC_DEF_INCOMPLETE_RESULT.getId()) {
                continue;
            }

            for (int j = 0; j < diagnostics.size(); j++) {
                if (i == j) {
                    continue;
                }

                var otherDiag = sourceResult.diags().get(diagnostics.get(j));

                if (otherDiag.id() != DiagnosticID.ERR_TYPECHECK_DECL_INCOMPLETE_TYPE.getId() &&
                        otherDiag.id() != DiagnosticID.ERR_FUNC_DEF_INCOMPLETE_RESULT.getId()) {
                    continue;
                }

                var identifier = getIdentifier(diag);
                var otherIdentifier = getIdentifier(otherDiag);

                if (identifier.equals(otherIdentifier)) {
                    diagnostics.remove(j);
                    j--;
                }
            }
        }
    }

    private void removeSimilarDiagnostics(List<Integer> diagnostics, DiagExporterSourceResult sourceResult) {
        removeStdIdentifierSpaceSimilarDiagnostics(diagnostics, sourceResult);
        removeTagSpaceSimilarDiagnostics(diagnostics, sourceResult);
    }

    // TODO remember that typedefs, variable names and function names share the same namespace
    // Tags (struct, enum, union) share the same namespace
    // Labels share the same namespace
    @Override
    public List<Integer> selectDiagnostics(DiagExporterSourceResult sourceResult, MendingTable mendingTable) {
        /*var missingPreprocessorFilesDiags = sourceResult.getDiagIdxs(DiagnosticID.ERR_PP_FILE_NOT_FOUND);

        // For now we first process these ones (we dont know how clang reports)
        if (!missingPreprocessorFilesDiags.isEmpty()) {
            removeExactlyRepeatedDiagnostics(missingPreprocessorFilesDiags, sourceResult);

            return missingPreprocessorFilesDiags;
        }

        var missingSymbolDiags = sourceResult.getDiagIdxs(
                Set.of(DiagnosticID.ERR_UNKNOWN_TYPENAME,
                        DiagnosticID.ERR_UNKNOWN_TYPENAME_SUGGEST,
                        DiagnosticID.ERR_UNDECLARED_VAR_USE,
                        DiagnosticID.ERR_UNDECLARED_VAR_USE_SUGGEST,
                        DiagnosticID.EXT_IMPLICIT_FUNCTION_DECL_C99,
                        DiagnosticID.ERR_NO_MEMBER, // Exact repeats of this one can be removed because the class is also named on the diagnostic
                        DiagnosticID.ERR_TYPECHECK_DECL_INCOMPLETE_TYPE,
                        DiagnosticID.ERR_FUNC_DEF_INCOMPLETE_RESULT));

        removeExactlyRepeatedDiagnostics(missingSymbolDiags, sourceResult);
        removeSimilarDiagnostics(missingSymbolDiags, sourceResult);

        if (!missingSymbolDiags.isEmpty()) {
            return missingSymbolDiags;
        }*/

        var missingPPFilesAndSymbolDiags = sourceResult.getDiagIdxs(
                Set.of(DiagnosticID.ERR_PP_FILE_NOT_FOUND,
                        DiagnosticID.ERR_UNKNOWN_TYPENAME,
                        DiagnosticID.ERR_UNKNOWN_TYPENAME_SUGGEST,
                        DiagnosticID.ERR_UNDECLARED_VAR_USE,
                        DiagnosticID.ERR_UNDECLARED_VAR_USE_SUGGEST,
                        DiagnosticID.EXT_IMPLICIT_FUNCTION_DECL_C99,
                        DiagnosticID.ERR_NO_MEMBER, // Exact repeats of this one can be removed because the class is also named on the diagnostic
                        DiagnosticID.ERR_TYPECHECK_DECL_INCOMPLETE_TYPE,
                        DiagnosticID.ERR_FUNC_DEF_INCOMPLETE_RESULT));

        removeExactlyRepeatedDiagnostics(missingPPFilesAndSymbolDiags, sourceResult);
        removeSimilarDiagnostics(missingPPFilesAndSymbolDiags, sourceResult);

        if (!missingPPFilesAndSymbolDiags.isEmpty()) {
            return missingPPFilesAndSymbolDiags;
        }

        return basicFirstErrorAnalysis.selectDiagnostics(sourceResult, mendingTable);
    }
}
