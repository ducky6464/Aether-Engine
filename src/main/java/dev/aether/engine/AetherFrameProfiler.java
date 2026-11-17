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

    public static synchronized void endFrame() {
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

    public static synchronized String report(int currentFps) {
        if (sampleCount == 0) {
            return enabled
                    ? "RetroGPU profiler: collecting samples; wait a few seconds and run /aether profile report again."
                    : "RetroGPU profiler: no samples. Run /aether profile start first.";
        }

        long[] sorted = new long[sampleCount];
        long total = 0L;
        long maximum = 0L;
        for (int i = 0; i < sampleCount; i++) {
            long sample = SAMPLES_NANOS[i];
            sorted[i] = sample;
            total += sample;
            maximum = Math.max(maximum, sample);
        }
        Arrays.sort(sorted);

        double averageMs = total / (double) sampleCount / 1_000_000.0;
        double p95Ms = percentile(sorted, 0.95) / 1_000_000.0;
        double p99Ms = percentile(sorted, 0.99) / 1_000_000.0;
        double maxMs = maximum / 1_000_000.0;

        Runtime runtime = Runtime.getRuntime();
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
        long maxMb = runtime.maxMemory() / (1024L * 1024L);

        return String.format(
                java.util.Locale.ROOT,
                "RetroGPU CPU frame profile (%d samples) | FPS=%d | avg=%.2fms | p95=%.2fms | p99=%.2fms | max=%.2fms | Java heap=%d/%dMB | GPU time not measured",
                sampleCount, currentFps, averageMs, p95Ms, p99Ms, maxMs, usedMb, maxMb);
    }

    private static long percentile(long[] sorted, double percentile) {
        int index = (int) Math.ceil(percentile * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
    }
}
