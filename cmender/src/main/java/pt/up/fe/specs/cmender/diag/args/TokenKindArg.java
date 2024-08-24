package pt.up.fe.specs.cmender.diag.args;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class TokenKindArg extends DiagnosticArg {
    private String name;
    private String spelling;
    private String category;
}
