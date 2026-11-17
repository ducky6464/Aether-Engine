package dev.aether.engine;

import java.util.Arrays;

/**
 * Low-overhead CPU-side frame profiler.
 *
 * Measures wall-clock time spent inside MinecraftClient.render(boolean).
 * This is not GPU execution time: OpenGL/VirGL may execute queued work later.
 */
public final class AetherFrameProfiler {
    private static final int CAPACITY = 300;
    private static final long[] SAMPLES_NANOS = new long[CAPACITY];

    private static volatile boolean enabled;
    private static long frameStartNanos;
    private static int cursor;
    private static int sampleCount;

    private AetherFrameProfiler() { }

    public static synchronized void start() {
        Arrays.fill(SAMPLES_NANOS, 0L);
        cursor = 0;
        sampleCount = 0;
        frameStartNanos = 0L;
        enabled = true;
    }

    public static synchronized void stop() {
        enabled = false;
        frameStartNanos = 0L;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void beginFrame() {
        if (enabled) {
            frameStartNanos = System.nanoTime();
        }
    }

    public static void endFrame() {
        if (!enabled) {
            return;
        }
        synchronized (AetherFrameProfiler.class) {
            if (!enabled || frameStartNanos == 0L) {
                return;
            }

            long elapsed = System.nanoTime() - frameStartNanos;
            frameStartNanos = 0L;
            if (elapsed <= 0L) {
                return;
            }

            SAMPLES_NANOS[cursor] = elapsed;
            cursor = (cursor + 1) % CAPACITY;
            if (sampleCount < CAPACITY) {
                sampleCount++;
            }
        }
    }

    public static synchronized String report(int currentFps) {
        if (sampleCount == 0) {
            return enabled
                    ? "RetroGPU profiler: collecting samples; wait a few seconds and run /aether profile report again."
                    : "RetroGPU profiler: no samples. Run /aether profile start first.";
        }

        long[] sorted = new long[sampleCount];
        long total = 0L;
        long maximum = 0L;
        int over16_67ms = 0;
        int over20ms = 0;
        int over33_33ms = 0;
        int over50ms = 0;
        for (int i = 0; i < sampleCount; i++) {
            long sample = SAMPLES_NANOS[i];
            sorted[i] = sample;
            total += sample;
            maximum = Math.max(maximum, sample);
            if (sample > 16_670_000L) over16_67ms++;
            if (sample > 20_000_000L) over20ms++;
            if (sample > 33_330_000L) over33_33ms++;
            if (sample > 50_000_000L) over50ms++;
        }
        Arrays.sort(sorted);

        double averageMs = total / (double) sampleCount / 1_000_000.0;
        double p95Ms = percentile(sorted, 0.95) / 1_000_000.0;
        double p99Ms = percentile(sorted, 0.99) / 1_000_000.0;
        double maxMs = maximum / 1_000_000.0;
        double averageFpsFromRenderTime = averageMs > 0.0 ? 1000.0 / averageMs : 0.0;

        Runtime runtime = Runtime.getRuntime();
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
        long maxMb = runtime.maxMemory() / (1024L * 1024L);

        return String.format(
                java.util.Locale.ROOT,
                "RetroGPU CPU frame profile (%d samples) | FPS now=%d | avg-render-rate=%.1f | avg=%.2fms | p95=%.2fms | p99=%.2fms | max=%.2fms | >16.67ms=%d | >20ms=%d | >33.33ms=%d | >50ms=%d | Java heap=%d/%dMB | GPU time not measured",
                sampleCount, currentFps, averageFpsFromRenderTime, averageMs, p95Ms, p99Ms, maxMs,
                over16_67ms, over20ms, over33_33ms, over50ms, usedMb, maxMb);
    }

    private static long percentile(long[] sorted, double percentile) {
        int index = (int) Math.ceil(percentile * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
    }
}
