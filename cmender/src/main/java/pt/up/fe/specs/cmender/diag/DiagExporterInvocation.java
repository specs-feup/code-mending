package pt.up.fe.specs.cmender.diag;

import lombok.*;
import pt.up.fe.specs.cmender.lang.Lang;
import pt.up.fe.specs.cmender.lang.Standard;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Builder
@ToString
@Getter
public class DiagExporterInvocation {
    @Builder.Default
    private List<String> files = new ArrayList<>();

    @Builder.Default
    private String outputFilepath = "./output.json";

    @Builder.Default
    private Lang lang = Lang.C;

    @Builder.Default
    private Standard standard = Standard.C11;

    @Builder.Default
    private int errorLimit = 0;

    public List<String> asInvocationArgs() {
        //return Arrays.stream(asInvocationArgsString().split(" ")).toList();
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
