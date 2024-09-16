package pt.up.fe.specs.cmender.diag;

import lombok.*;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.lang.Lang;
import pt.up.fe.specs.cmender.lang.Standard;

import java.util.List;
import java.util.ArrayList;

@Builder
@ToString
@Getter
@Accessors(fluent = true)
public class DiagExporterInvocation {
    @Builder.Default
    private List<String> files = new ArrayList<>();

    @Builder.Default
    private String outputFilepath = "./cmender_diags_output.json";

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
        args.add("--");
        args.add("-x");
        args.add(lang.getClangInvocationSpelling());
        args.add("-std=" + standard.getClangInvocationSpelling());
        args.add("-ferror-limit=" + errorLimit);

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
                "-o %s -- -x %s -std=%s -ferror-limit=%d".formatted(
                        outputFilepath,
                        lang.getClangInvocationSpelling(),
                        standard.getClangInvocationSpelling(),
                        errorLimit));

        return stringBuilder.toString();
    }
}
