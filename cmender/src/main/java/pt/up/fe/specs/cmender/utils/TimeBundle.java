package pt.up.fe.specs.cmender.utils;

public record TimeBundle(
        long nanos,
        double millis,
        double seconds,
        double ratio,
        double percent
) {
    public static TimeBundle fromNanos(long nanos, long totalTime) {
        return new TimeBundle(
                nanos,
                TimeMeasure.milliseconds(nanos),
                TimeMeasure.seconds(nanos),
                TimeMeasure.ratio(totalTime, nanos),
                TimeMeasure.percentage(totalTime, nanos)
        );
    }

    public static TimeBundle fromNanos(long nanos) {
        return new TimeBundle(
                nanos,
                TimeMeasure.milliseconds(nanos),
                TimeMeasure.seconds(nanos),
                1.0,
                100.0
        );
    }
}

