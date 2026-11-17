package dev.aether.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AetherChunkUploadProfilerTest {
    @Test
    void recordsChunkUploadCpuTimeAndStops() throws InterruptedException {
        AetherChunkUploadProfiler.start();
        try {
            AetherChunkUploadProfiler.beginUpload();
            Thread.sleep(2L);
            AetherChunkUploadProfiler.endUpload();

            String report = AetherChunkUploadProfiler.report();
            assertTrue(report.contains("(1 samples)"), report);
            assertTrue(report.contains("avg="), report);
            assertTrue(report.contains("p95="), report);
            assertTrue(report.contains("GPU completion time not measured"), report);
        } finally {
            AetherChunkUploadProfiler.stop();
        }
    }

    @Test
    void reportsWhenNoSamplesHaveBeenCollected() {
        AetherChunkUploadProfiler.start();
        try {
            assertTrue(AetherChunkUploadProfiler.report().contains("collecting samples"));
        } finally {
            AetherChunkUploadProfiler.stop();
        }
    }
}
