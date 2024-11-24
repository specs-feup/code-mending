package pt.up.fe.specs.cmender.mending;

import lombok.Builder;
import pt.up.fe.specs.cmender.utils.SizeBundle;
import pt.up.fe.specs.cmender.utils.TimeBundle;

import java.util.List;

@Builder(toBuilder = true)
public record SourceResult(
        String sourceFile,

        SizeBundle fileSize,

        boolean success,

        MendingEngineFatalException fatalException,

        long iterationCount,

        double completionStatusEstimate,

        SizeBundle mendfileSize,
        TimeBundle totalTime,
        TimeBundle diagExporterTotalTime,
        TimeBundle otherTotalTime,

        List<DiagnosticShortInfo> unknownDiags,

        List<MendingIterationResult> mendingIterations
) { }
