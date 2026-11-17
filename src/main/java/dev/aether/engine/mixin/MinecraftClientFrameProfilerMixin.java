package dev.aether.engine.mixin;

import dev.aether.engine.AetherFrameProfiler;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Samples the CPU-side duration of Minecraft's complete client render method. */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientFrameProfilerMixin {
    @Inject(method = "render(Z)V", at = @At("HEAD"))
    private void retrogpu$beginFrameProfile(boolean tick, CallbackInfo ci) {
        AetherFrameProfiler.beginFrame();
    }

    @Inject(method = "render(Z)V", at = @At("RETURN"))
    private void retrogpu$endFrameProfile(boolean tick, CallbackInfo ci) {
        AetherFrameProfiler.endFrame();
    }
}
