package pt.up.fe.specs.cmender.mending;

import lombok.Builder;
import pt.up.fe.specs.cmender.CMenderInvocation;

import java.util.List;
import java.util.Map;

@Builder
public record CMenderReport(
        CMenderInvocation invocation,

        long totalFiles,
        //long totalMendableFiles, // TODO (distinguish) files that were not mendable because they were already correct

        long successCount,
        //long trueSuccessCount, // TODO (distinguish) files that were successfully mended and were not correct from the start
        //long correctFromStartCount,
        long unsuccessfulCount,
        long fatalExceptionCount,

        double successRatio,
        //double trueSuccessRate,
        //double correctFromStartRate,
        double unsuccessfulRatio,
        double fatalExceptionRatio,
        double fatalExceptionOverUnsuccessfulRatio,

        Map<String, Integer> unknownDiagsFrequency,
        long uniqueUnknownDiagsCount,

        List<String> nullMendingDirs,

        List<SourceResult> sourceResults
) { }
