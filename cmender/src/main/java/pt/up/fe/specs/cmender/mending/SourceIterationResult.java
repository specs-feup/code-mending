package pt.up.fe.specs.cmender.mending;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record SourceIterationResult(
        List<DiagnosticResultInfo> diags,
        DiagnosticMendResult mendResult,

        // Iteration times in NS
        long time,
        long diagExporterTime,
        long mendingTime,
        long mendfileWritingTime,
        long otherTime,

        // Iteration times in MS
        double timeMs,
        double diagExporterTimeMs,
        double mendingTimeMs,
        double mendfileWritingTimeMs,
        double otherTimeMs,

        // Percentage of iteration times
        double diagExporterTimePercentage,
        double mendingTimePercentage,
        double mendfileWritingTimePercentage,
        double otherTimePercentage
) { }
