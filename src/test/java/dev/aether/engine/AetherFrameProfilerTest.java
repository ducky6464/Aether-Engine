package dev.aether.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AetherFrameProfilerTest {
    @Test
    void keepsOnlyTheMostRecentThreeHundredSamples() {
        AetherFrameProfiler.start();
        try {
            for (int i = 0; i < 305; i++) {
                AetherFrameProfiler.beginFrame();
                AetherFrameProfiler.endFrame();
            }
            assertTrue(AetherFrameProfiler.report(60).contains("(300 samples)"));
        } finally {
            AetherFrameProfiler.stop();
        }
    }

    @Test
    void restartingProfilerClearsPreviousSamples() {
        AetherFrameProfiler.start();
        AetherFrameProfiler.beginFrame();
        AetherFrameProfiler.endFrame();
        AetherFrameProfiler.stop();

        AetherFrameProfiler.start();
        try {
            assertTrue(AetherFrameProfiler.report(60).contains("collecting samples"));
        } finally {
            AetherFrameProfiler.stop();
        }
    }

    @Test
    void recordsCpuFrameSamplesAndStopsCleanly() throws InterruptedException {
        AetherFrameProfiler.start();
        try {
            AetherFrameProfiler.beginFrame();
            Thread.sleep(2L);
            AetherFrameProfiler.endFrame();

            String report = AetherFrameProfiler.report(60);
            assertTrue(report.contains("(1 samples)"), report);
            assertTrue(report.contains("avg="), report);
            assertTrue(report.contains("p95="), report);
            assertTrue(report.contains("avg-render-rate="), report);
            assertTrue(report.contains(">16.67ms="), report);
            assertTrue(report.contains(">33.33ms="), report);
            assertTrue(report.contains("GPU time not measured"), report);
        } finally {
            AetherFrameProfiler.stop();
        }

        assertTrue(AetherFrameProfiler.report(60).contains("(1 samples)"));
    }
}
