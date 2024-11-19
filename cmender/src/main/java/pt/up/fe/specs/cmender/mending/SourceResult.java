package pt.up.fe.specs.cmender.mending;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record SourceResult(
        String sourceFile,

        long lineCount,

        boolean success,

        //boolean correctFromStart,

        MendingEngineFatalException fatalException,

        long iterations,

        double completionScore,

        List<DiagnosticShortInfo> unknownDiags,

        List<MendingIterationResult> iterationResults,

        // Total times in NS
        long totalTime,
        long diagExporterTotalTime,
        long mendingTotalTime,
        long mendfileWritingTotalTime,
        long otherTotalTime,

        // Total times in MS
        double totalTimeMs,
        double diagExporterTotalTimeMs,
        double mendingTotalTimeMs,
        double mendfileWritingTotalTimeMs,
        double otherTotalTimeMs,

        // Percentage of total times
        double diagExporterTotalTimePercentage,
        double mendingTotalTimePercentage,
        double mendfileWritingTotalTimePercentage,
        double otherTotalTimePercentage
) {
    /*public boolean isTrueSuccess() {
        return success && !correctFromStart;
    }

    public boolean isTrueUnsuccessful() {
        return !success && !correctFromStart;
    }*/
}
