package dev.aether.engine.mixin;

import dev.aether.engine.AetherEngine;
import dev.aether.engine.AetherPlayerRenderEligibility;
import dev.aether.engine.AetherPlayerRenderQueue;
import dev.aether.engine.AetherPlayerWorldRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Shadow protected EntityModel<?> model;

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At("HEAD")
    )
    private void aether$captureBasePlayerState(LivingEntityRenderState state,
                                                MatrixStack matrices,
                                                OrderedRenderCommandQueue queue,
                                                CameraRenderState cameraState,
                                                CallbackInfo ci) {
        if (AetherPlayerWorldRenderer.shouldCapturePlayerSnapshots()
                && state instanceof PlayerEntityRenderState playerState) {
            AetherEngine.playerRenderQueue().capture(playerState);
        }
    }

    /**
     * Captures the posed base model at the exact vanilla submission point.
     * Vanilla is skipped only for ordinary players after Aether has completed
     * at least one successful mirror frame. Unsupported or uncaptured players
     * always retain the vanilla base-model command.
     */
    @Redirect(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/texture/Sprite;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V"
            )
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void aether$submitOrReplaceBaseModel(
            OrderedRenderCommandQueue queue,
            Model submittedModel,
            Object submittedState,
            MatrixStack matrices,
            RenderLayer renderLayer,
            int light,
            int overlay,
            int tintedColor,
            Sprite sprite,
            int outlineColor,
            ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {

        if (AetherPlayerWorldRenderer.shouldCapturePlayerSnapshots()
                && submittedState instanceof PlayerEntityRenderState playerState
                && model instanceof PlayerEntityModel playerModel
                && AetherPlayerWorldRenderer.captureAndShouldSuppressVanilla(
                        playerState, playerModel, matrices)) {
            return;
        }

        queue.submitModel(submittedModel, submittedState, matrices, renderLayer,
                light, overlay, tintedColor, sprite, outlineColor, crumblingOverlay);
    }
}
