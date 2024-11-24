package pt.up.fe.specs.cmender.utils;

import java.util.function.Supplier;

public class TimeMeasure {

    public static long measureElapsed(Runnable runnable) {
        long timeStart = System.nanoTime();

        runnable.run();

        return System.nanoTime() - timeStart;
    }

    public static <T> TimedResult<T> measureElapsed(Supplier<T> supplier) {
        long timeStart = System.nanoTime();

        T result = supplier.get();

        var elapsedTime = System.nanoTime() - timeStart;

        return new TimedResult<>(result, elapsedTime);
    }

    public static double milliseconds(long nanoseconds) {
        return nanoseconds / 1_000_000.0;
    }

    public static double seconds(long nanoseconds) {
        return nanoseconds / 1_000_000_000.0;
    }

    public static double ratio(long totalTime, long time) {
        return (double) time / totalTime;
    }

    public static double ratio(long totalTime, long time, int iterations) {
        return ratio(totalTime, time) / iterations;
    }

    public static double percentage(long totalTime, long time) {
        return (double) time / totalTime * 100;
    }

    // average percentage per iteration
    public static double percentage(long totalTime, long time, int iterations) {
        return percentage(totalTime, time) / iterations;
    }
}
