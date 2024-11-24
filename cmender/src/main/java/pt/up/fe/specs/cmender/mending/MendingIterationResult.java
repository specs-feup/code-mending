package pt.up.fe.specs.cmender.mending;

import lombok.Builder;
import pt.up.fe.specs.cmender.utils.SizeBundle;
import pt.up.fe.specs.cmender.utils.TimeBundle;

import java.util.List;

@Builder(toBuilder = true)
public record MendingIterationResult(
        MendingTerminationStatus terminationStatus,
        List<Integer> selectedDiagnostics,
        long errorCount,
        long fatalCount,
        DiagnosticMendResult mendResult,

        SizeBundle mendfileSize,
        TimeBundle totalTime,
        TimeBundle diagExporterTime,
        TimeBundle otherTime
) { }
