package dev.aether.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AetherPlayerRenderEligibilityTest {
    @Test
    void ordinaryPlayerUsesCustomBaseModel() {
        assertTrue(AetherPlayerRenderEligibility.canUseCustomBaseModel(0));
    }

    @Test
    void capturedPosesUseCustomBaseModel() {
        int[] supportedPoseFlags = {
                AetherPlayerRenderQueue.BABY,
                AetherPlayerRenderQueue.SNEAKING,
                AetherPlayerRenderQueue.SWIMMING,
                AetherPlayerRenderQueue.GLIDING,
                AetherPlayerRenderQueue.SLEEPING,
                AetherPlayerRenderQueue.UPSIDE_DOWN,
                AetherPlayerRenderQueue.APPLY_FLYING_ROTATION,
                AetherPlayerRenderQueue.ON_FIRE
        };

        for (int flag : supportedPoseFlags) {
            assertTrue(AetherPlayerRenderEligibility.canUseCustomBaseModel(flag),
                    "Expected captured pose/overlay flag to remain eligible: " + flag);
        }
    }

    @Test
    void unsupportedVisibilityAndEffectStatesStayVanilla() {
        int[] fallbackFlags = {
                AetherPlayerRenderQueue.SPECTATOR,
                AetherPlayerRenderQueue.INVISIBLE,
                AetherPlayerRenderQueue.INVISIBLE_TO_PLAYER,
                AetherPlayerRenderQueue.HURT,
                AetherPlayerRenderQueue.SHAKING,
                AetherPlayerRenderQueue.OUTLINE,
                AetherPlayerRenderQueue.EXTRA_EARS
        };

        for (int flag : fallbackFlags) {
            assertFalse(AetherPlayerRenderEligibility.canUseCustomBaseModel(flag),
                    "Expected unsupported state to remain vanilla: " + flag);
        }
    }

    @Test
    void unsupportedStateWinsWhenCombinedWithSupportedPose() {
        int flags = AetherPlayerRenderQueue.SNEAKING | AetherPlayerRenderQueue.HURT;
        assertEquals(AetherPlayerRenderEligibility.Route.VANILLA_SPECIAL_EFFECT,
                AetherPlayerRenderEligibility.classify(flags));
    }
}
