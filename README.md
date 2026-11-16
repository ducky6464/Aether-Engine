# Aether Engine

A standalone experimental Fabric client mod for Minecraft 1.21.11.

## Current status

- Standalone Fabric Loom project targeting Minecraft 1.21.11 and Java 21.
- Player base-model capture, geometry staging, skin grouping, GPU upload, and custom draw pass.
- Opt-in replacement for eligible ordinary player base models, armed only after a successful custom validation frame.
- Unsupported player states and feature layers remain vanilla.
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
gradle build
```

The distributable jar is written to `build/libs/`.
