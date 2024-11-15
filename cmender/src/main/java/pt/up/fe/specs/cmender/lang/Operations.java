package pt.up.fe.specs.cmender.lang;

import java.util.Objects;

public class Operations {

    public static boolean isComparison(String op) {
        return switch (op) {
            case "==", "!=", "<", ">", "<=", ">=" -> true;
            default -> false;
        };
    }

    public static boolean isBinarySum(String op) {
        return Objects.equals(op, "+");
    }

    public static boolean isBinarySub(String op) {
        return Objects.equals(op, "-");
    }
}
