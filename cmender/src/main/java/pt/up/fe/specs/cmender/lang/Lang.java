package pt.up.fe.specs.cmender.lang;

import lombok.Getter;

@Getter
public enum Lang {
    C("C"),
    CPP("C++");

    private final String spelling;

    Lang(String spelling) {
        this.spelling = spelling;
    }

    public static Lang toLang(String lang) {
        if (lang == null || lang.isEmpty()) {
            throw new IllegalArgumentException("Illegal null or empty Lang name.");
        }

        lang = lang.toLowerCase();

        return switch (lang) {
            case "c" -> C;
            case "c++", "cxx", "cpp" -> CPP;
            default -> null;
        };
    }

    public String getClangInvocationSpelling() {
        return this.spelling.toLowerCase();
    }
}
