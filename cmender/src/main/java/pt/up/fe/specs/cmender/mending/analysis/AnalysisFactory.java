package pt.up.fe.specs.cmender.mending.analysis;

import java.util.List;

public class AnalysisFactory {

    public static final List<String> ANALYSIS_TYPES = List.of(
            BasicFirstErrorAnalysis.class.getSimpleName(),
            BasicMultipleErrorsAnalysis.class.getSimpleName(),
            BasicMultiplePPErrorsAnalysis.class.getSimpleName()
    );

    public static boolean isValidAnalysisType(String analysisType) {
        return ANALYSIS_TYPES.contains(analysisType);
    }

    public static DiagnosticAnalysis createAnalysis(String analysisType) {
        if (analysisType.equals(BasicFirstErrorAnalysis.class.getSimpleName())) {
            return new BasicFirstErrorAnalysis();
        } else if (analysisType.equals(BasicMultipleErrorsAnalysis.class.getSimpleName())) {
            return new BasicMultipleErrorsAnalysis();
        } else if (analysisType.equals(BasicMultiplePPErrorsAnalysis.class.getSimpleName())) {
            return new BasicMultiplePPErrorsAnalysis();
        } else {
            return null;
        }
    }
}
