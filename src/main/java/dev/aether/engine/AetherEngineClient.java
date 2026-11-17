package dev.aether.engine;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AetherEngineClient implements ClientModInitializer {
    public static final String MOD_ID = "aether_engine";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        try {
            AetherPlayerPipeline.register();
            LOGGER.info("Aether player pipeline registered; vanilla rendering remains authoritative until replacement is armed.");
        } catch (RuntimeException exception) {
            LOGGER.warn("Aether pipeline registration failed; custom rendering will remain unavailable.", exception);
        }

        WorldRenderEvents.BEFORE_ENTITIES.register(context -> AetherEngine.beginFrame());
        WorldRenderEvents.AFTER_ENTITIES.register(context -> AetherEngine.endFrame());
        AetherPlayerWorldRenderer.register();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread()) {
                AetherEngine.close();
            } else {
                client.execute(AetherEngine::close);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var root = ClientCommandManager.literal("aether");
            root.then(ClientCommandManager.literal("mirror")
                    .then(ClientCommandManager.literal("on").executes(context -> {
                        AetherPlayerWorldRenderer.setMirrorEnabled(true);
                        context.getSource().sendFeedback(Text.literal("Aether mirror enabled; vanilla still renders."));
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("off").executes(context -> {
                        AetherPlayerWorldRenderer.setMirrorEnabled(false);
                        context.getSource().sendFeedback(Text.literal("Aether mirror disabled."));
                        return 1;
                    })));
            root.then(ClientCommandManager.literal("replace")
                    .then(ClientCommandManager.literal("on").executes(context -> {
                        AetherPlayerWorldRenderer.setReplacementEnabled(true);
                        context.getSource().sendFeedback(Text.literal(
                                "Aether replacement is arming. Vanilla stays active until a custom frame succeeds."));
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("off").executes(context -> {
                        AetherPlayerWorldRenderer.setReplacementEnabled(false);
                        context.getSource().sendFeedback(Text.literal(
                                "Aether replacement disabled. Vanilla base-model rendering restored."));
                        return 1;
                    })));
            root.then(ClientCommandManager.literal("profile")
                    .then(ClientCommandManager.literal("start").executes(context -> {
                        AetherFrameProfiler.start();
                        context.getSource().sendFeedback(Text.literal(
                                "RetroGPU profiler started. Collect at least 30 seconds in a repeatable scene."));
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("stop").executes(context -> {
                        AetherFrameProfiler.stop();
                        context.getSource().sendFeedback(Text.literal("RetroGPU profiler stopped."));
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("report").executes(context -> {
                        context.getSource().sendFeedback(Text.literal(
                                AetherFrameProfiler.report(MinecraftClient.getInstance().getCurrentFps())));
                        return 1;
                    })));
            var chunks = ClientCommandManager.literal("chunks").executes(context -> {
                MinecraftClient client = MinecraftClient.getInstance();
                var renderer = client.worldRenderer;
                var chunkBuilder = renderer.getChunkBuilder();
                if (chunkBuilder == null) {
                    context.getSource().sendFeedback(Text.literal("Aether chunks | chunk builder unavailable (not in a loaded world)."));
                    return 1;
                }
                String report = String.format(
                        java.util.Locale.ROOT,
                        "Aether chunks | scheduledTasks=%d | pendingUploads=%d | freeBuffers=%d | completedChunks=%d | chunkCount=%.0f | %s",
                        chunkBuilder.getScheduledTaskCount(),
                        chunkBuilder.getChunksToUpload(),
                        chunkBuilder.getFreeBufferCount(),
                        renderer.getCompletedChunkCount(),
                        renderer.getChunkCount(),
                        chunkBuilder.getDebugString());
                context.getSource().sendFeedback(Text.literal(report));
                return 1;
            });
            chunks.then(ClientCommandManager.literal("profile")
                    .then(ClientCommandManager.literal("start").executes(context -> {
                        AetherChunkUploadProfiler.start();
                        context.getSource().sendFeedback(Text.literal(
                                "Chunk upload CPU profiler started. Collect a repeatable scene for 30 seconds."));
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("stop").executes(context -> {
                        AetherChunkUploadProfiler.stop();
                        context.getSource().sendFeedback(Text.literal("Chunk upload CPU profiler stopped."));
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("report").executes(context -> {
                        context.getSource().sendFeedback(Text.literal(AetherChunkUploadProfiler.report()));
                        return 1;
                    })));
            root.then(chunks);
            root.then(ClientCommandManager.literal("stats").executes(context -> {
                context.getSource().sendFeedback(Text.literal(
                        "Aether | mirror=" + AetherPlayerWorldRenderer.isMirrorEnabled()
                                + " | replaceRequested=" + AetherPlayerWorldRenderer.isReplacementRequested()
                                + " | replaceReady=" + AetherPlayerWorldRenderer.isReplacementReady()
                                + " | suppressed=" + AetherPlayerWorldRenderer.vanillaBaseModelsSuppressed()
                                + " | captured=" + AetherEngine.playerRenderQueue().size()
                                + " | geometry=" + AetherEngine.playerGeometryArena().capturedPlayers()
                                + " | packets=" + AetherEngine.playerGeometryDrawPlan().packetCount()
                                + " | status=" + AetherPlayerWorldRenderer.lastStatus()));
                return 1;
            }));
            dispatcher.register(root);
        });

        LOGGER.info("Aether Engine initialized for Minecraft 1.21.11.");
    }
}
