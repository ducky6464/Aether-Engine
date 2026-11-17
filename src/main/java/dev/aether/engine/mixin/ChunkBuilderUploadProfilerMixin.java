package dev.aether.engine.mixin;

import dev.aether.engine.AetherChunkUploadProfiler;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Times CPU work performed by the vanilla chunk-upload queue drain. */
@Mixin(ChunkBuilder.class)
public abstract class ChunkBuilderUploadProfilerMixin {
    @Inject(method = "upload()V", at = @At("HEAD"))
    private void aether$beginChunkUploadProfile(CallbackInfo ci) {
        AetherChunkUploadProfiler.beginUpload();
    }

    @Inject(method = "upload()V", at = @At("RETURN"))
    private void aether$endChunkUploadProfile(CallbackInfo ci) {
        AetherChunkUploadProfiler.endUpload();
    }
}
