package pt.up.fe.specs.cmender.mending;

import lombok.Builder;
import pt.up.fe.specs.cmender.CMenderInvocation;

import java.util.List;

@Builder
public record CMenderResult(
        CMenderInvocation invocation,
        List<SourceResult> sourceResults
) { }
