# create-squaremap-bridge

[**中文文档**](README_CN.md) | [**English**](#)

A lightweight Fabric server-side mod that solves the problem of [Create Fly](https://github.com/ZurrTum/Create-Fly) block changes not being reflected on the [squaremap](https://github.com/jpenilla/squaremap) web map.

## What it does

Create's contraptions write blocks directly into chunk storage, bypassing standard block update events — so squaremap never gets notified and the map goes stale. This mod bridges the gap: it captures every `setBlockState` call and forwards it to squaremap's `CHUNK_CHANGED` event.

- Contraption assembly/disassembly, mining heads, track laying and item transport show up on the map automatically
- Liquid flow is filtered out by default to avoid render spam
- Per-world queues keep overworld / nether / end updates isolated
- Safe by design: all exceptions are swallowed, `defaultRequire: 0` means it degrades gracefully instead of crashing on future MC versions

## Requirements

| Component | Version |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.18.x |
| squaremap | 1.3.12+ |
| create-fly | 6.0.9-5 |

## Building

Requires JDK 21+.

```bash
./gradlew build
# Output: build/libs/create-squaremap-bridge-fabric-mc1.21.11-1.0.0.jar
```

## Installation

1. Copy the jar from `build/libs/` into your server's `mods/` folder
2. Restart the server
3. No mixin errors in the startup log means it loaded successfully

## Configuration

Config file: `config/create-squaremap-bridge.properties` (auto-generated on first start)

| Option | Default | Description |
|---|---|---|
| `flush-interval-ticks` | `40` | Batch flush interval in ticks (20 ticks = 1s). Higher = less frequent re-renders (saves CPU/network), slower map updates |
| `filter-liquids` | `true` | Filter liquid-to-liquid flow (water/lava) from triggering re-renders |
| `debug-log` | `false` | Print the number of chunks flushed per batch to the server log |
| `language` | `en` | Console log language: `en` or `zh` |

> Changes take effect after a server restart.
>
> Tip: squaremap's own `background-render.interval-seconds` (default 15) is the final render cadence — `flush-interval-ticks` cannot make updates faster than that.

## How it works

```
Create writes a block → mixin catches World/WorldChunk.setBlockState
→ block actually changed? → dedupe per chunk (flush every 2s)
→ fire squaremap CHUNK_CHANGED event → chunk re-rendered
```

## Compatibility

- Statically verified against all 15 mods on the target server (Lithium / create-fly / fabric-api / journeymap, etc.) — no mixin conflicts
- **Note**: depends on squaremap's internal class `xyz.jpenilla.squaremap.fabric.event.MapUpdateEvents` (not a public API). May require small adaptations on major squaremap upgrades; verified stable within the 1.3.x line
- No fabric-api runtime dependency (tick hook implemented via mixin)

## License

[MIT](LICENSE)
