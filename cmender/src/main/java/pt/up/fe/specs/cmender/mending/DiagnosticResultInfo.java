package pt.up.fe.specs.cmender.mending;

public record DiagnosticResultInfo(
        String id,
        String message
) {
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
