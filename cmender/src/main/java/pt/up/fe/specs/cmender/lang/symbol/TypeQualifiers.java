package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
@Builder
@Accessors(fluent = true)
public class TypeQualifiers {

    @Builder.Default
    private boolean hasConst = false;

    @Builder.Default
    private boolean hasConstPtr = false;
}
