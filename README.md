# ThreadCraft

Fabric performance mod for Minecraft 26.2 that parallelizes world generation and chunk meshing off the main thread. Works alongside rendering mods like Sodium — it handles CPU-side mesh/generation work, Sodium keeps the GPU/render pipeline.

## What it does

- Runs chunk generation (noise, biomes, features, structures) on a background thread pool.
- Builds chunk meshes (blocks → vertex data) off-thread on a separate pool.
- Runs lighting propagation on its own pool.
- Installs finished chunks/meshes back on the main thread so nothing touches live world data from worker threads.
- Caps queued tasks per pool (back-pressure) so flying fast doesn't OOM the client.

## What it isn't

- Not a replacement for Sodium, Lithium, etc. Different stage of the pipeline.
- Not a 5x speedup guarantee. Serial game logic still limits gains (Amdahl's Law).
- Fabric only. No Forge/NeoForge.

## Requirements

- Fabric Loader `>=0.17.0`
- Minecraft `~26.2`
- Java `>=21`

## Install

Drop `threadcraft-1.0.jar` into your `mods/` folder alongside Fabric Loader and Fabric API.

## Config

A `threadcraft.properties` file appears in your config directory on first run:

| Setting | Default | What it does |
|---------|---------|--------------|
| `generationThreads` | CPU cores | Threads for chunk generation |
| `meshingThreads` | CPU cores | Threads for chunk meshing |
| `lightingThreads` | CPU cores / 2 | Threads for lighting |
| `maxPendingChunks` | threads × 4 | Cap on queued generation tasks |
| `enableAsyncGen` | `true` | Toggle async world generation |
| `enableAsyncMesh` | `true` | Toggle async chunk meshing |

Lower thread counts if you're on a single-core CPU or seeing instability.

## In-game settings

ThreadCraft adds its own **Threadcraft** button directly to Minecraft's main settings menu:

**Settings → Threadcraft**

Click it to open ThreadCraft's config screen with async toggles and thread sliders, then click **Save**.

ThreadCraft also works with **Mod Menu + Cloth Config** — find ThreadCraft in the Mod Menu list and click **Config** as an alternative entry point.

Settings are saved to `config/threadcraft.properties` and applied immediately.

ThreadCraft does **not** replace Sodium's settings. Per plan.md §6, they're compatible but separate: Sodium handles GPU/rendering, ThreadCraft handles CPU generation/meshing. They operate on different pipeline stages.


## Build

```
./gradlew build
```

Output lands in `build/libs/`.

## Roadmap (from plan.md)

1. Foundation — mod init, config, thread pools, basic async generation
2. World generation — full async pipeline, lighting, back-pressure, benchmarks
3. Chunk meshing — immutable chunk snapshots, off-thread mesh building, GPU upload on main thread
4. Polish — config GUI, compatibility checks with Sodium/Lithium/Entity Culling, bug fixes

## License

MIT — open source, modpack-friendly.
