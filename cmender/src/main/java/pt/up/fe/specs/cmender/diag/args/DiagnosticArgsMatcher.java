package pt.up.fe.specs.cmender.diag.args;

import java.util.List;

public class DiagnosticArgsMatcher {
    public static boolean match(List<? extends DiagnosticArg> args, List<Class<? extends DiagnosticArg>> expectedTypes) {
        if (args.size() != expectedTypes.size()) {
            return false;
        }

        for (int i = 0; i < args.size(); i++) {
            if (!expectedTypes.get(i).isInstance(args.get(i))) {
                return false;
            }

        }

        return true;
    }
}
