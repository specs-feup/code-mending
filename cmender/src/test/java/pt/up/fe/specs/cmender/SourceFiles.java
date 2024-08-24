package pt.up.fe.specs.cmender;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class SourceFiles {

    public static final String INCOMPATIBLE_TYPES_C = "incompatible_types.c";
    public static final String MISSING_FUNCTIONS_C = "missing_functions.c";
    public static final String MISSING_HEADERS_C = "missing_headers.c";
    public static final String MISSING_VARS_C = "missing_vars.c";
    public static final String RECURSIVE_MACRO_FUNCTION_C = "recursive_macro_function.c";
    public static final String EMPTY_MAIN_C = "empty_main.c";

    public static String getSourceFilepath(String sourceFilename) {
        if (!Arrays.stream(allSourceFilenames()).toList().contains(sourceFilename)) {
            throw new IllegalArgumentException("Source file '" + sourceFilename + "' does not exist");
        }

        // var classLoader = SourceFiles.class.getClassLoader();
        // var sourceUrl = classLoader.getResource("sources/" + sourceFilename);

        var sourceUrl = SourceFiles.class.getResource("/sources/" + sourceFilename);

        if (sourceUrl == null) {
            throw new IllegalArgumentException("Source file '" + sourceFilename + "' does not exist");
        }

        try {
            return new File(sourceUrl.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<String> getSourceFilepaths(String ...sourceFilenames) {
        return Arrays.stream(sourceFilenames).map(SourceFiles::getSourceFilepath).toList();
    }

    // TODO improve this to avoid having to update the method every time we add a new source file
    public static String[] allSourceFilenames() {
        return new String[] {
          INCOMPATIBLE_TYPES_C,
          MISSING_FUNCTIONS_C,
          MISSING_HEADERS_C,
          MISSING_VARS_C,
          RECURSIVE_MACRO_FUNCTION_C,
                EMPTY_MAIN_C};
    }
}
