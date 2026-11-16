# Aether Engine testing

## Current scope

Aether is a standalone Fabric client mod for Minecraft 1.21.11 / Java 21.

The current implementation captures ordinary player base-model geometry and can submit an experimental mirror pass. It does **not** replace Minecraft's renderer. Vanilla rendering, player feature layers, held items, capes, armor, and special player states remain authoritative.

## In-game checks

1. Install the built Aether Engine jar with Fabric Loader 0.18.2+ and Fabric API for 1.21.11.
2. Launch a world and check that startup completes without a Mixin error.
3. Run `/aether stats` and verify capture counts update while players are rendered.
4. Run `/aether mirror on` to enable the experimental duplicate base-model pass.
5. Run `/aether mirror off` to disable it.
6. Test with multiple player skins and observe transparent skin pixels, depth behavior, and frame stability.
7. Check the log for Aether upload, fence, shader, or render-pass errors.

Mirror mode is disabled by default. Do not use performance results from mirror mode as evidence of a faster production renderer: it intentionally draws an additional copy while vanilla remains active.

## Known limitations

- No production renderer handoff or vanilla draw suppression.
- Feature layers and held items are still vanilla.
- The mirror path has not yet been validated in a real multiplayer world.
- Performance gains have not been measured.
