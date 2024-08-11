package pt.up.fe.specs.cmender.lang;

import lombok.Getter;

@Getter
public enum Standard {
    C11("C11");

    private final String spelling;

    Standard(String spelling) {
        this.spelling = spelling;
    }

    public String getClangInvocationSpelling() {
        return this.spelling.toLowerCase();
    }
}
