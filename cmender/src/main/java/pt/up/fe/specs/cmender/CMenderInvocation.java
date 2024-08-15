package pt.up.fe.specs.cmender;

import lombok.Builder;
import lombok.ToString;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@ToString
@Getter
public class CMenderInvocation {

    @Builder.Default
    private List<String> command = new ArrayList<>();;

    @Builder.Default
    private boolean version = false;

    @Builder.Default
    private boolean help = false;

    @Builder.Default
    private boolean verbose = false;

    @Builder.Default
    private String diagExporterPath = null;

    @Builder.Default
    private List<String> files = new ArrayList<>();
}
