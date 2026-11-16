# Aether Engine testing

## Current scope

Aether is a standalone Fabric client mod for Minecraft 1.21.11 / Java 21.

The replacement path captures the posed player base model at the same point vanilla submits it. This allows the custom base-model path to handle ordinary standing, baby-scaled, sneaking, swimming, gliding, sleeping, upside-down, and flying-rotation poses. Hurt/shaking, invisible, spectator, outline, and extra-ear states stay on vanilla. Armor, capes, held items, name tags, fire overlays, and other feature layers remain vanilla.

## Automated checks

Run:

```sh
gradle test
gradle build
```

The tests cover player-state routing and the compact CPU-to-GPU vertex packing contract.

## In-game checks

1. Install the built Aether Engine jar with Fabric Loader 0.18.2+ and Fabric API for 1.21.11.
2. Launch a test world and verify startup completes without a Mixin error.
3. Run `/aether stats` and verify player capture counts update.
4. Run `/aether mirror on` first. Confirm the extra player model is visually aligned, textured, lit, and depth-tested. Run `/aether mirror off` afterward.
5. Run `/aether replace on`. Aether first keeps vanilla rendering while it validates a complete custom draw frame. Once armed, eligible player base models are submitted by Aether instead of vanilla.
6. Run `/aether stats` and inspect `replaceReady`, `suppressed`, and `status`.
7. Run `/aether replace off` immediately if any player base model disappears, flickers, has incorrect positioning, skin transparency, lighting, or depth behavior.
8. Test standing, sneaking, swimming, gliding, sleeping, baby-sized, and upside-down player poses, plus multiple skins, resource reloads, world changes, and multiplayer players.

## Fallback behavior and limitations

- Replacement is opt-in and OFF by default.
- The first successful custom frame is a validation frame; vanilla remains active during that frame.
- Only eligible base models are replaced. Feature layers and unsupported player states stay vanilla.
- If the custom pass reports a failure, Aether disarms replacement so vanilla rendering resumes on the following frame. A GPU failure can still cause a one-frame visual gap because the ordered vanilla command has already been skipped for that frame.
- This is experimental and has not yet been validated in a real Minecraft world or multiplayer session.
- No performance gains are claimed. Measure only after visual correctness is confirmed.
