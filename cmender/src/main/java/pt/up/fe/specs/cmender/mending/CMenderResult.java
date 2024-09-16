package pt.up.fe.specs.cmender.mending;

import lombok.Builder;

@Builder(toBuilder = true)
public record CMenderResult(
        boolean success,

        // Total time in NS
        long totalTime,
        long diagExporterTotalTime,
        long mendingTotalTime,
        long mendfileWritingTotalTime,
        long otherTotalTime,

        // Total time in MS
        double totalTimeMs,
        double diagExporterTotalTimeMs,
        double mendingTotalTimeMs,
        double mendfileWritingTotalTimeMs,
        double otherTotalTimeMs,

        // Percentage of total time
        double diagExporterTotalTimePercentage,
        double mendingTotalTimePercentage,
        double mendfileWritingTotalTimePercentage,
        double otherTotalTimePercentage,

        long iterations,
        String unknownDiag // TODO change when we support the tolerance of more than one unknown diag/multiple files
) { }
