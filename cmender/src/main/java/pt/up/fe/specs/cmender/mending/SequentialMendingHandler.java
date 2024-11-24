package pt.up.fe.specs.cmender.mending;

import pt.up.fe.specs.cmender.data.MendingDirData;
import pt.up.fe.specs.cmender.diag.Diagnostic;
import pt.up.fe.specs.cmender.diag.DiagnosticID;

import java.util.List;

public class SequentialMendingHandler implements MendingHandler {

    @Override
    public DiagnosticMendResult mend(List<Diagnostic> selectedDiags, MendingTable mendingTable, MendingDirData mendingDirData) {
        for (var diag : selectedDiags) {
            System.out.println(">>> " + diag);
            mendingTable.handledDiagnostics().add(diag);


            var diagnosticID = DiagnosticID.fromIntID(diag.id());

            switch (diagnosticID) {
                case DiagnosticID.EXT_IMPLICIT_FUNCTION_DECL_C99 ->            declareFunctionHeuristic(diag, mendingTable);
                case DiagnosticID.ERR_UNDECLARED_VAR_USE ->                    declareVariableHeuristic(diag, mendingTable);
                case DiagnosticID.ERR_UNDECLARED_VAR_USE_SUGGEST ->            declareVariableSuggestHeuristic(diag, mendingTable);
                case DiagnosticID.ERR_TYPECHECK_CONVERT_INCOMPATIBLE ->        adjustConversionTypesHeuristic(diag, mendingTable);
                case DiagnosticID.ERR_TYPECHECK_INVALID_OPERANDS ->            adjustOperandTypesHeuristic(diag, mendingTable);
                case DiagnosticID.ERR_PP_FILE_NOT_FOUND ->                     createHeaderFileHeuristic(diag, mendingTable, mendingDirData);
                case DiagnosticID.ERR_TYPECHECK_DECL_INCOMPLETE_TYPE ->        defineTagTypeHeuristic(diag, mendingTable);
                case DiagnosticID.ERR_NO_MEMBER ->                             addStructMemberHeuristic(diag, mendingTable);
                case DiagnosticID.ERR_UNKNOWN_TYPENAME ->                      createTypedefTypeAliasHeuristic(diag, mendingTable);
                case DiagnosticID.ERR_UNKNOWN_TYPENAME_SUGGEST ->              createTypedefTypeAliasSuggestHeuristic(diag, mendingTable);
                case DiagnosticID.ERR_TYPECHECK_MEMBER_REFERENCE_SUGGESTION -> adjustMemberReferenceHeuristic(diag, mendingTable);
                case DiagnosticID.ERR_TYPECHECK_SUBSCRIPT_VALUE ->             adjustSubscriptBaseHeuristic(diag, mendingTable);
                case DiagnosticID.ERR_TYPECHECK_SUBSCRIPT_NOT_INTEGER ->       convertSubscriptToIntegerHeuristic(diag, mendingTable);
                default -> { }
            }
        }

        return DiagnosticMendResult.builder().build();
    }
}
