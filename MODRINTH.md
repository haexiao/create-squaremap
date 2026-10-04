# Create Squaremap

A lightweight Fabric server-side mod that bridges **Create Fly** block changes to **squaremap** re-rendering.

Create's contraptions write blocks directly into chunk storage, bypassing standard block update events — so squaremap never gets notified and the web map goes stale. This mod captures every `setBlockState` call and forwards it to squaremap's `CHUNK_CHANGED` event.

**中文文档**: [README_CN](https://github.com/haexiao/create-squaremap/blob/master/README_CN.md)

## Features

- Contraption assembly/disassembly, mining heads, track laying and item transport show up on the map automatically
- Liquid flow filtered out by default (no render spam)
- Per-world queues keep overworld / nether / end updates isolated
- Safe by design: all exceptions are swallowed, degrades gracefully instead of crashing on future MC versions
- Configurable via `config/create-squaremap.properties`

## Requirements

| Component | Version |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.18.x |
| squaremap | 1.3.12+ |
| create-fly | 6.0.9-5 |

## Configuration

Config file: `config/create-squaremap.properties` (auto-generated on first start)

| Option | Default | Description |
|---|---|---|
| `flush-interval-ticks` | `40` | Batch flush interval in ticks (20 ticks = 1s). Higher = less frequent re-renders, slower map updates |
| `filter-liquids` | `true` | Filter liquid-to-liquid flow from triggering re-renders |
| `debug-log` | `false` | Print flushed chunk counts to the server log |
| `language` | `en` | Console log language: `en` or `zh` |

> Changes take effect after a server restart.

## Installation

1. Copy the jar into your server's `mods/` folder
2. Restart the server
3. No mixin errors in the startup log means it loaded successfully

## Note

Depends on squaremap's internal class `MapUpdateEvents` (not a public API) — may require small adaptations on major squaremap upgrades; stable within the 1.3.x line.

Source: [github.com/haexiao/create-squaremap](https://github.com/haexiao/create-squaremap)
