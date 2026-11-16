# Aether Engine

A standalone experimental Fabric client mod for Minecraft 1.21.11.

## Current status

- Standalone Fabric Loom project targeting Minecraft 1.21.11 and Java 21.
- Player base-model capture, geometry staging, skin grouping, GPU upload, and custom draw pass.
- Opt-in replacement for eligible player base models, armed only after a successful custom validation frame.
- Captured poses include standing, baby-scaled, sneaking, swimming, gliding, sleeping, upside-down, and flying-rotation states.
- Hurt/shaking, invisible, spectator, outline, and extra-ear states remain vanilla.
- Armor, capes, held items, name tags, fire overlays, and other feature layers remain vanilla.
- Automated routing and vertex-packing tests run as part of the Gradle build.
- Replacement is experimental and has not yet been validated in a real Minecraft world.
- No performance improvement is claimed until measured in-world.

## Commands

- `/aether stats`
- `/aether mirror on|off`
- `/aether replace on|off`

Replacement is OFF by default. See [AETHER_TESTING.md](AETHER_TESTING.md) before enabling it.

## Build

Use Java 21 and Gradle 9.4.0 or compatible:

```sh
gradle test
gradle build
```

The distributable jar is written to `build/libs/`.
