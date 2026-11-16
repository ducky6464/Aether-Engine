package dev.aether.engine;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
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
            LOGGER.info("Aether player pipeline registered; vanilla rendering remains authoritative.");
        } catch (RuntimeException exception) {
            LOGGER.warn("Aether pipeline registration failed; mirror mode will remain unavailable.", exception);
        }

        WorldRenderEvents.START.register(context -> AetherEngine.beginFrame());
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
            var mirror = ClientCommandManager.literal("mirror")
                    .then(ClientCommandManager.literal("on").executes(context -> {
                        AetherPlayerWorldRenderer.setMirrorEnabled(true);
                        context.getSource().sendFeedback(Text.literal(
                                "Aether mirror enabled (experimental; vanilla still renders)."));
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("off").executes(context -> {
                        AetherPlayerWorldRenderer.setMirrorEnabled(false);
                        context.getSource().sendFeedback(Text.literal("Aether mirror disabled."));
                        return 1;
                    }));
            var stats = ClientCommandManager.literal("stats").executes(context -> {
                context.getSource().sendFeedback(Text.literal(
                        "Aether | mirror=" + AetherPlayerWorldRenderer.isMirrorEnabled()
                                + " | captured=" + AetherEngine.playerRenderQueue().size()
                                + " | geometry=" + AetherEngine.playerGeometryArena().capturedPlayers()
                                + " | packets=" + AetherEngine.playerGeometryDrawPlan().packetCount()
                                + " | status=" + AetherPlayerWorldRenderer.lastStatus()));
                return 1;
            });
            root.then(mirror);
            root.then(stats);
            dispatcher.register(root);
        });

        LOGGER.info("Aether Engine initialized for Minecraft 1.21.11.");
    }
}
