package dev.aether.engine;

import java.util.Arrays;

/** Opt-in CPU timing for Minecraft's chunk upload phase. */
public final class AetherChunkUploadProfiler {
    private static final int CAPACITY = 300;
    private static final long[] SAMPLES_NANOS = new long[CAPACITY];
    private static volatile boolean enabled;
    private static long startNanos;
    private static int cursor;
    private static int count;

    private AetherChunkUploadProfiler() { }

    public static synchronized void start() {
        Arrays.fill(SAMPLES_NANOS, 0L);
        cursor = 0;
        count = 0;
        startNanos = 0L;
        enabled = true;
    }

    public static synchronized void stop() {
        enabled = false;
        startNanos = 0L;
    }

    public static void beginUpload() {
        if (enabled) startNanos = System.nanoTime();
    }

    public static void endUpload() {
        if (!enabled) return;
        synchronized (AetherChunkUploadProfiler.class) {
            if (!enabled || startNanos == 0L) return;
            long elapsed = System.nanoTime() - startNanos;
            startNanos = 0L;
            if (elapsed <= 0L) return;
            SAMPLES_NANOS[cursor] = elapsed;
            cursor = (cursor + 1) % CAPACITY;
            if (count < CAPACITY) count++;
        }
    }

    public static synchronized String report() {
        if (count == 0) {
            return enabled
                    ? "Aether chunk upload profiler: collecting samples; wait a few seconds and report again."
                    : "Aether chunk upload profiler: no samples. Run /aether chunks profile start first.";
        }
        long[] sorted = Arrays.copyOf(SAMPLES_NANOS, count);
        long total = 0L;
        long max = 0L;
        int over1ms = 0;
        int over4ms = 0;
        int over8ms = 0;
        for (long sample : sorted) {
            total += sample;
            max = Math.max(max, sample);
            if (sample > 1_000_000L) over1ms++;
            if (sample > 4_000_000L) over4ms++;
            if (sample > 8_000_000L) over8ms++;
        }
        Arrays.sort(sorted);
        double averageMs = total / (double) count / 1_000_000.0;
        double p95Ms = sorted[Math.max(0, (int) Math.ceil(count * 0.95) - 1)] / 1_000_000.0;
        return String.format(java.util.Locale.ROOT,
                "Aether chunk upload CPU profile (%d samples) | avg=%.3fms | p95=%.3fms | max=%.3fms | >1ms=%d | >4ms=%d | >8ms=%d | GPU completion time not measured",
                count, averageMs, p95Ms, max / 1_000_000.0, over1ms, over4ms, over8ms);
    }
}
