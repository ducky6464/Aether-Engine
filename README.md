# Aether Engine

A standalone experimental Fabric client mod for Minecraft 1.21.11.

Aether is being developed as a separate project from LowSpec RenderCore. It contains the player base-model capture, geometry staging, batching, GPU upload, and experimental mirror-rendering prototype.

## Current status

- Standalone Fabric Loom project targeting Minecraft 1.21.11 and Java 21.
- Experimental player base-model capture and opt-in mirror pass.
- Vanilla rendering remains authoritative. The mirror draws an additional copy and is disabled by default.
- Feature layers, armor, held items, capes, and special player states remain vanilla.
- No performance improvement is claimed until measured in-world.

## Commands

- `/aether stats`
- `/aether mirror on`
- `/aether mirror off`

See [AETHER_TESTING.md](AETHER_TESTING.md) for test instructions and known limitations.

## Build

Use Java 21 and Gradle 9.2.1 or a compatible Gradle installation:

```sh
gradle build
```

The distributable jar is written to `build/libs/`.
