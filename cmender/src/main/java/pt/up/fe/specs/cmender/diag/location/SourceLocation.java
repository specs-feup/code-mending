package pt.up.fe.specs.cmender.diag.location;

public record SourceLocation(
        int line,
        int column,
        String file,
        String path,
        String encompassingCode
) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SourceLocation other)) {
            return false;
        }
        return line == other.line
                && column == other.column
                && file.equals(other.file)
                && path.equals(other.path);
    }
}
