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
    private List<String> invocation = new ArrayList<>();;

    @Builder.Default
    private boolean version = false;

    @Builder.Default
    private boolean help = false;

    @Builder.Default
    private boolean verbose = false;

    @Builder.Default
    private String diagExporterPath = null;

    @Builder.Default
    private Integer maxTotalIterations = 10;

    @Builder.Default
    private boolean createMendfileCopyPerIteration = false;

    @Builder.Default
    private boolean createMendfileOnlyOnAlterations = false;

    // this allows to possibly reach a better state, or stop if the code is too broken
    @Builder.Default
    private boolean continueOnUnknownDiagnostic = false;

    @Builder.Default
    private String output = "./output";

    // TODO: Add support for mending in place (i.e., declarations on the source file)
    /*@Builder.Default
    private boolean mendingInPlace = false;*/

    @Builder.Default
    private List<String> files = new ArrayList<>();
}
