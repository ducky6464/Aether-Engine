package dev.aether.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AetherPlayerWorldRendererTest {
    @AfterEach
    void resetRendererModes() {
        AetherPlayerWorldRenderer.setReplacementEnabled(false);
        AetherPlayerWorldRenderer.setMirrorEnabled(false);
    }

    @Test
    void snapshotCollectionIsDisabledDuringNormalVanillaRendering() {
        AetherPlayerWorldRenderer.setMirrorEnabled(false);
        AetherPlayerWorldRenderer.setReplacementEnabled(false);

        assertFalse(AetherPlayerWorldRenderer.shouldCapturePlayerSnapshots());
    }

    @Test
    void snapshotCollectionIsEnabledForMirrorMode() {
        AetherPlayerWorldRenderer.setMirrorEnabled(true);

        assertTrue(AetherPlayerWorldRenderer.shouldCapturePlayerSnapshots());
    }

    @Test
    void snapshotCollectionIsEnabledForReplacementMode() {
        AetherPlayerWorldRenderer.setReplacementEnabled(true);

        assertTrue(AetherPlayerWorldRenderer.shouldCapturePlayerSnapshots());
    }
}
