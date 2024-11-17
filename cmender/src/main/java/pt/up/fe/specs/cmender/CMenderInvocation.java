package pt.up.fe.specs.cmender;

import lombok.Builder;
import lombok.ToString;
import lombok.Getter;
import pt.up.fe.specs.cmender.diag.DiagExporterInvocation;

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
    private Integer maxTotalIterations = 1000;

    // If this is set, we create mendfile copies and export them in the output directory
    // Another option for 'exportMendfileCopyPerIteration' needless here
    @Builder.Default
    private boolean createMendfileCopyPerIteration = false;

    @Builder.Default
    private boolean createMendfileOnlyOnAlterations = false;

    // If this is set, we create diag output copies and export them in the output directory
    @Builder.Default
    private boolean createDiagsOutputCopyPerIteration = false;

    // this allows to possibly reach a better state, or stop if the code is too broken
    @Builder.Default
    private boolean continueOnUnknownDiagnostic = false;

    @Builder.Default
    private String output = "./output";

    @Builder.Default
    private boolean outputDiagsOutput = false;

    @Builder.Default
    private String diagsOutputFilename = "cmender_diags_output.json"; // TODO

    @Builder.Default
    private String resultFilename = "cmender_result.json"; // TODO

    // TODO: Add support for mending in place (i.e., declarations on the source file)
    /*@Builder.Default
    private boolean mendingInPlace = false;*/

    @Builder.Default
    private List<String> files = new ArrayList<>();
}
