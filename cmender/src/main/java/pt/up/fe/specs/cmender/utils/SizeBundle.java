package pt.up.fe.specs.cmender.utils;

public record SizeBundle(
        long bytes,
        double kilobytes,
        double megabytes
) {
    public static SizeBundle fromBytes(long bytes) {
        return new SizeBundle(bytes, bytes / 1024.0, bytes / 1024.0 / 1024.0);
    }
}
