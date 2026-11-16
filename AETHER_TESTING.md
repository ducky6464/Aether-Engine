# Aether Engine testing

## Current scope

Aether is a standalone Fabric client mod for Minecraft 1.21.11 / Java 21.

The first replacement path handles only ordinary, visible, standing player base models. Player feature layers (armor, capes, held items, name tags, and other overlays) remain vanilla. Spectators, invisible players, special poses/effects, outlines, and unsupported model implementations remain vanilla.

## In-game checks

1. Install the built Aether Engine jar with Fabric Loader 0.18.2+ and Fabric API for 1.21.11.
2. Launch a test world and verify startup completes without a Mixin error.
3. Run `/aether stats` and verify player capture counts update.
4. Run `/aether mirror on` first. Confirm the extra player model is visually aligned, textured, lit, and depth-tested. Run `/aether mirror off` afterward.
5. Run `/aether replace on`. Aether first keeps vanilla rendering while it validates a complete custom draw frame. Once armed, eligible ordinary player base models are submitted by Aether instead of vanilla.
6. Run `/aether stats` and inspect `replaceReady`, `suppressed`, and `status`.
7. Run `/aether replace off` immediately if any player base model disappears, flickers, has incorrect positioning, skin transparency, lighting, or depth behavior.
8. Test with multiple skins, resource reloads, world changes, and multiplayer players.

## Fallback behavior and limitations

- Replacement is opt-in and OFF by default.
- The first successful custom frame is a validation frame; vanilla remains active during that frame.
- Only eligible base models are replaced. Feature layers and unsupported player states stay vanilla.
- If the custom pass reports a failure, Aether disarms replacement so vanilla rendering resumes on the following frame. A GPU failure can still cause a one-frame visual gap because the ordered vanilla command has already been skipped for that frame.
- This is experimental and has not yet been validated in a real Minecraft world or multiplayer session.
- No performance gains are claimed. Measure only after visual correctness is confirmed.
