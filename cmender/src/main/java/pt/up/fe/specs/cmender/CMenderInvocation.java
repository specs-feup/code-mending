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
    private int threads = 1;

    @Builder.Default
    private String diagExporterPath = null;

    @Builder.Default
    private Integer maxTotalIterations = 1000;

    @Builder.Default
    private Integer maxTotalTime = 10; // minutes

    @Builder.Default
    private String analysis = "BasicFirstErrorAnalysis";

    @Builder.Default
    private String handler = "BasicSequentialMendingHandler";

    @Builder.Default
    private boolean noDisclaimer = false;

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
    private boolean reportPerSource = false;

    @Builder.Default
    private String output = "./output";

    @Builder.Default
    private boolean outputDiagsOutput = false;

    @Builder.Default     // because "mending" is the process, not the result. "mends" is the result
    private String mendfileFilename = "cmender_mends.h"; // TODO

    @Builder.Default
    private String diagsOutputFilename = "cmender_diags_output.json"; // TODO

    @Builder.Default
    private String reportFilename = "cmender_report.json"; // TODO

    @Builder.Default
    private String sourceReportFilename = "source_report.json"; // TODO

    // TODO: Add support for mending in place (i.e., declarations on the source file)
    /*@Builder.Default
    private boolean mendingInPlace = false;*/

    @Builder.Default
    private List<String> files = new ArrayList<>();
}
