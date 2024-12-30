package pt.up.fe.specs.cmender.diag;

import lombok.*;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.data.CMenderDataManager;
import pt.up.fe.specs.cmender.lang.Lang;
import pt.up.fe.specs.cmender.lang.Standard;

import java.util.List;
import java.util.ArrayList;

@Builder
@ToString
@Getter
@Accessors(fluent = true)
public class DiagExporterInvocation {

    // public static final String DEFAULT_OUTPUT_FILENAME_NO_EXT = "cmender_diags_output";

    public static final String DEFAULT_OUTPUT_FILENAME = "diag_exporter_diags_output.json";

    @Builder.Default
    private List<String> includePaths = new ArrayList<>();

    @Builder.Default
    private List<String> files = new ArrayList<>();

    @Builder.Default
    private String outputFilepath = "./" + DEFAULT_OUTPUT_FILENAME; // the "./" is not necessary, but it is added for clarity

    @Builder.Default
    private String severityMappingFilepath = CMenderDataManager.DIAGNOSTIC_SEVERITY_MAPPING_FILEPATH;

    @Builder.Default
    private Lang lang = Lang.C;

    @Builder.Default
    private Standard standard = Standard.C11;

    @Builder.Default
    private int errorLimit = 0;

    @Builder.Default
    private boolean isVersion = false;

    @Builder.Default
    private boolean isHelp = false;

    public List<String> asInvocationArgs() {
        //return Arrays.stream(asInvocationArgsString().split(" ")).toList();

        if (isVersion) {
            return new ArrayList<>(List.of("--version"));
        }

        if (isHelp) {
            return new ArrayList<>(List.of("--help"));
        }
        
        var args = new ArrayList<>(files);

        args.add("-o");
        args.add(outputFilepath);
        args.add("-m");
        args.add(severityMappingFilepath);
        args.add("--");
        args.add("-x");
        args.add(lang.getClangInvocationSpelling());
        args.add("-std=" + standard.getClangInvocationSpelling());
        args.add("-ferror-limit=" + errorLimit);
        // TODO add some feature to include target to user specified libs or just libc/libc++
        args.add("-nostdinc"); // disable standard include paths
        args.add("-isysroot"); // disable system root
        args.add("");

        for (String includePath : includePaths) {
            args.add("-I"+includePath);
        }

        return args;
    }

    public String asInvocationArgsString() {
        if (isVersion) {
            return "--version";
        }

        if (isHelp) {
            return "--help";
        }

        var stringBuilder = new StringBuilder();

        for (String file : files) {
            stringBuilder.append(file).append(" ");
        }

        stringBuilder.append(
                "-o %s -m %s -- -x %s -std=%s -ferror-limit=%d -nostdinc -isysroot \"\"".formatted(
                        outputFilepath,
                        severityMappingFilepath,
                        lang.getClangInvocationSpelling(),
                        standard.getClangInvocationSpelling(),
                        errorLimit))
                .append(" ");

        for (String includePath : includePaths) {
            stringBuilder.append("-I").append(includePath).append(" ");
        }

        return stringBuilder.toString();
    }
}
