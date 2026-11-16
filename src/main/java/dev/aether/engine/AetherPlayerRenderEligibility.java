package dev.aether.engine;

/**
 * Conservative routing policy for the base-player model renderer.
 *
 * The model geometry is captured at vanilla's base-model submission point,
 * after the renderer has applied the current pose and entity transform. Pose
 * flags therefore do not require a separate custom animation implementation.
 * Effects that alter vanilla's color/visibility path still use vanilla.
 */
public final class AetherPlayerRenderEligibility {
    public enum Route {
        CUSTOM_BASE_MODEL,
        VANILLA_SPECTATOR,
        VANILLA_INVISIBLE,
        VANILLA_SPECIAL_POSE,
        VANILLA_SPECIAL_EFFECT,
        VANILLA_OUTLINE,
        VANILLA_EXTRA_GEOMETRY
    }

    private AetherPlayerRenderEligibility() { }

    public static Route classify(int flags) {
        if ((flags & AetherPlayerRenderQueue.SPECTATOR) != 0) return Route.VANILLA_SPECTATOR;
        if ((flags & (AetherPlayerRenderQueue.INVISIBLE
                | AetherPlayerRenderQueue.INVISIBLE_TO_PLAYER)) != 0) {
            return Route.VANILLA_INVISIBLE;
        }
        if ((flags & (AetherPlayerRenderQueue.HURT
                | AetherPlayerRenderQueue.SHAKING)) != 0) {
            return Route.VANILLA_SPECIAL_EFFECT;
        }
        if ((flags & AetherPlayerRenderQueue.OUTLINE) != 0) return Route.VANILLA_OUTLINE;
        if ((flags & AetherPlayerRenderQueue.EXTRA_EARS) != 0) return Route.VANILLA_EXTRA_GEOMETRY;

        // Baby scale, sneaking, swimming, gliding, sleeping, upside-down and
        // flying rotations are already reflected in the posed ModelPart tree
        // and the MatrixStack by the time vanilla submits the base model.
        // Fire is a separate feature overlay; it remains vanilla-rendered.
        return Route.CUSTOM_BASE_MODEL;
    }

    public static boolean canUseCustomBaseModel(int flags) {
        return classify(flags) == Route.CUSTOM_BASE_MODEL;
    }
}
