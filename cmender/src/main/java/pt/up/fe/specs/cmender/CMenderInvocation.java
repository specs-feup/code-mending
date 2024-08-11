package pt.up.fe.specs.cmender;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Builder
@ToString
@Getter
public class CMenderInvocation {

    private List<String> command;

    @Builder.Default
    private boolean verbose = false;

    @Builder.Default
    private String diagExporterPath = null;

    @Builder.Default
    private List<String> files = new ArrayList<>();
}
