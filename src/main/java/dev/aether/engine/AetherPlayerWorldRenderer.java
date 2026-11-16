package dev.aether.engine;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Experimental base-player renderer. Replacement is opt-in and is armed only
 * after a complete custom mirror frame has succeeded. Unsupported player states
 * and failed geometry captures continue through Minecraft's vanilla renderer.
 */
public final class AetherPlayerWorldRenderer {
    private static final Vector4f WHITE = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f ZERO_OFFSET = new Vector3f();
    private static final Matrix4f IDENTITY_TEXTURE = new Matrix4f();
    private static volatile boolean mirrorEnabled;
    private static volatile boolean replacementRequested;
    private static volatile boolean replacementReady;
    private static volatile long successfulFrames;
    private static volatile long submittedPackets;
    private static volatile long skippedFrames;
    private static volatile long failedFrames;
    private static volatile long vanillaBaseModelsSuppressed;
    private static volatile String lastStatus = "disabled";

    private AetherPlayerWorldRenderer() { }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> drawAfterEntities());
    }

    public static void setMirrorEnabled(boolean enabled) {
        mirrorEnabled = enabled;
        if (!replacementRequested) {
            lastStatus = enabled ? "enabled; experimental mirror pass" : "disabled";
        }
    }

    public static void setReplacementEnabled(boolean enabled) {
        replacementRequested = enabled;
        replacementReady = false;
        mirrorEnabled = enabled;
        lastStatus = enabled
                ? "arming replacement; vanilla remains active until a successful custom frame"
                : "replacement disabled; vanilla base models restored";
    }

    public static boolean isMirrorEnabled() { return mirrorEnabled; }
    public static boolean isReplacementRequested() { return replacementRequested; }
    public static boolean isReplacementReady() { return replacementReady; }
    public static long successfulFrames() { return successfulFrames; }
    public static long submittedPackets() { return submittedPackets; }
    public static long skippedFrames() { return skippedFrames; }
    public static long failedFrames() { return failedFrames; }
    public static long vanillaBaseModelsSuppressed() { return vanillaBaseModelsSuppressed; }
    public static String lastStatus() { return lastStatus; }

    /**
     * Called at vanilla's base-model queue submission point. Geometry must be
     * captured successfully and the skin must be resolvable before suppression.
     */
    public static boolean captureAndShouldSuppressVanilla(
            PlayerEntityRenderState state, PlayerEntityModel model, MatrixStack matrices) {
        if (state == null || model == null || matrices == null) return false;
        AetherPlayerRenderQueue snapshots = AetherEngine.playerRenderQueue();
        int index = snapshots.size() - 1;
        if (index < 0 || snapshots.playerId(index) != state.id) return false;
        if (!AetherPlayerRenderEligibility.canUseCustomBaseModel(snapshots.flags(index))) return false;

        boolean captured = AetherEngine.capturePlayerGeometry(index, model.getRootPart(), matrices);
        if (!captured || !replacementRequested || !replacementReady
                || !AetherPlayerPipeline.isRegistered()) {
            return false;
        }
        if (AetherPlayerTextureBinder.resolve(snapshots.skin(index)) == null) return false;
        vanillaBaseModelsSuppressed++;
        return true;
    }

    private static void drawAfterEntities() {
        if (!mirrorEnabled && !replacementRequested) return;
        if (!RenderSystem.isOnRenderThread()) {
            replacementReady = false;
            skippedFrames++;
            lastStatus = "skipped: not on render thread; vanilla fallback next frame";
            return;
        }
        AetherPlayerRenderQueue queue = AetherEngine.playerRenderQueue();
        if (queue.size() == 0) {
            replacementReady = false;
            skippedFrames++;
            lastStatus = "skipped: no player snapshots; vanilla fallback next frame";
            return;
        }

        GpuBuffer vertexBuffer = AetherEngine.preparePlayerGeometryForDraw();
        AetherPlayerGeometryDrawPlan plan = AetherEngine.playerGeometryDrawPlan();
        if (vertexBuffer == null || plan.packetCount() == 0) {
            replacementReady = false;
            skippedFrames++;
            lastStatus = "skipped: no uploaded player geometry; vanilla fallback next frame";
            return;
        }

        Framebuffer target = MinecraftClient.getInstance().getFramebuffer();
        if (target == null || target.getColorAttachmentView() == null || target.getDepthAttachmentView() == null) {
            replacementReady = false;
            skippedFrames++;
            lastStatus = "skipped: main framebuffer attachments unavailable; vanilla fallback next frame";
            return;
        }

        int submitted = 0;
        try {
            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().write(
                    RenderSystem.getModelViewMatrix(), WHITE, ZERO_OFFSET, IDENTITY_TEXTURE);
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            try (RenderPass pass = encoder.createRenderPass(
                    () -> replacementRequested ? "Aether player replacement pass" : "Aether player mirror pass",
                    target.getColorAttachmentView(),
                    OptionalInt.empty(),
                    target.getDepthAttachmentView(),
                    OptionalDouble.empty())) {
                if (AetherPlayerPipeline.pipeline() == null) {
                    replacementReady = false;
                    skippedFrames++;
                    lastStatus = "skipped: Aether pipeline not registered; vanilla fallback next frame";
                    return;
                }
                for (int skinGroup = 0; skinGroup < queue.skinGroupCount(); skinGroup++) {
                    submitted += AetherPlayerDrawSubmitter.submitSkinBatch(
                            pass, vertexBuffer, queue, plan, skinGroup, transforms);
                }
            }
            if (submitted > 0) {
                AetherEngine.markPlayerGeometryDrawSubmitted(vertexBuffer);
                successfulFrames++;
                submittedPackets += submitted;
                replacementReady = replacementRequested;
                lastStatus = replacementRequested
                        ? "replacement pass succeeded; eligible base models use Aether, unsupported states stay vanilla"
                        : "submitted " + submitted + " player base-model ranges; vanilla still active";
            } else {
                replacementReady = false;
                skippedFrames++;
                lastStatus = "skipped: no skin batch submitted; vanilla fallback next frame";
            }
        } catch (RuntimeException exception) {
            replacementReady = false;
            failedFrames++;
            lastStatus = "failed: " + exception.getClass().getSimpleName() + ": " + exception.getMessage()
                    + "; vanilla fallback next frame";
            if (failedFrames == 1 || failedFrames % 100 == 0) {
                AetherEngineClient.LOGGER.warn(
                        "Aether experimental player render pass failed (failures={})", failedFrames, exception);
            }
        }
    }
}
