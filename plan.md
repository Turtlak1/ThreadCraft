# ThreadCraft — Fabric Performance Mod Plan

**Mod ID:** `threadcraft`  
**Display Name:** ThreadCraft  
**Package:** `com.threadcraft`  
**Target:** Minecraft 26.2 + Fabric Loader  
**Compatibility:** Sodium, Lithium, Entity Culling, Starlight, and other Fabric mods

---

## 1. Vision

ThreadCraft is a Fabric-side performance mod that makes Minecraft faster by parallelizing the CPU-bound subsystems that currently run on the single-threaded main game loop. It works **alongside** rendering mods like Sodium (which handles GPU/rendering optimizations) rather than replacing them.

**Goal:** Measurable FPS and world-generation speedups on multi-core CPUs, without breaking compatibility with the Fabric mod ecosystem.

---

## 2. What ThreadCraft Is (and Isn't)

### Is
- A Fabric mod (`.jar`, loads via Fabric Loader)
- Multi-threading for world generation, chunk meshing, and data packing
- Compatible with Sodium (rendering), Lithium (game logic), Entity Culling, etc.
- Configurable thread counts and task queues

### Isn't
- A replacement for Sodium (rendering pipeline stays with Sodium)
- A rewrite of Minecraft in Rust
- A 5x speedup guarantee (Amdahl's Law — serial game logic limits max gain)
- Compatible with Forge/NeoForge (Fabric only)

---

## 3. Performance Targets (Realistic)

| Area | Expected Improvement | Notes |
|------|---------------------|-------|
| World generation (new terrain) | 2–4x | Scales with CPU cores |
| Chunk mesh building | 1.5–2x | Off-thread block→vertex conversion |
| FPS (exploring new areas) | 10–30% | Less stutter from gen/mesh spikes |
| FPS (standing still) | 0–5% | Main thread still handles game logic |
| Startup time | Minimal | Not a target |

---

## 4. Architecture

### Thread Pools

| Pool | Purpose | Thread Count | Thread Name Prefix |
|------|---------|--------------|-------------------|
| `generationPool` | Chunk generation (noise, biomes, features, structures) | `availableProcessors()` | `threadcraft-gen-N` |
| `meshingPool` | Chunk mesh building (block → vertex data) | `availableProcessors()` | `threadcraft-mesh-N` |
| `lightingPool` | Sky light / block light propagation | `availableProcessors() / 2` | `threadcraft-light-N` |
| `dataPackPool` | Data packing / network packet serialization | `availableProcessors()` | `threadcraft-data-N` |

### Thread Safety Rules (Non-Negotiable)

1. **Immutable snapshots only** — Worker threads receive copies/immutable views of chunk data, never live references
2. **Main thread installs** — Only `Minecraft.getInstance()` / `ServerLevel` thread installs generated chunks into the world
3. **GPU on main thread** — All OpenGL/Vulkan calls stay on the render thread (Sodium handles this)
4. **Back-pressure** — Cap pending tasks per pool (e.g., 2× thread count) to prevent OOM when flying fast

---

## 5. Core Systems

### 5.1 Async Chunk Generation

**Problem:** `ChunkGenerator` and `ChunkSource` generate chunks on the main thread, causing hitches when exploring.

**Solution:**
- Wrap chunk generation in `CompletableFuture.supplyAsync(generationPool)`
- Main thread submits generation requests, polls for completion
- Chunks installed into `LevelChunk` only on the main thread
- Lighting calculation can run in parallel on `lightingPool`

**Files to touch:**
- `net/minecraft/world/level/chunk/ChunkSource.java` — add `generationExecutor`
- `net/minecraft/world/level/chunk/ChunkGenerator.java` — split generation into async stages
- `net/minecraft/server/level/ChunkMap.java` — async chunk delivery

### 5.2 Async Chunk Meshing

**Problem:** `LevelRenderer` rebuilds chunk meshes on the main render thread, causing FPS drops when turning.

**Solution:**
- Capture immutable `ChunkSnapshot` (block states, light, data)
- Submit mesh building to `meshingPool`
- Main render thread uploads finished meshes to GPU (via Sodium/Vanilla render pipeline)

**Files to touch:**
- `net/minecraft/client/renderer/LevelRenderer.java` — async mesh rebuild
- `net/minecraft/client/renderer/SectionBufferBuilder.java` — off-thread buffer building

### 5.3 Config System

Fabric config API (Cloth Config or built-in Fabric config):

| Setting | Default | Description |
|---------|---------|-------------|
| `generationThreads` | `availableProcessors()` | Worker threads for chunk generation |
| `meshingThreads` | `availableProcessors()` | Worker threads for chunk meshing |
| `lightingThreads` | `availableProcessors() / 2` | Worker threads for lighting |
| `maxPendingChunks` | `threads * 4` | Cap on queued generation tasks |
| `enableAsyncGen` | `true` | Toggle async world generation |
| `enableAsyncMesh` | `true` | Toggle async chunk meshing |

---

## 6. Compatibility Layer

### Sodium
- ThreadCraft does **not** touch rendering pipeline, shaders, or GPU state
  - Sodium handles: vertex buffer rendering, GPU upload, culling, occlusion
  - ThreadCraft handles: CPU-side mesh data generation
- They operate on different stages of the pipeline — fully compatible

### Lithium
- Lithium optimizes: entity AI, ticking, mob spawning, block ticking
- ThreadCraft optimizes: chunk generation, meshing
- Non-overlapping — can run together

### Entity Culling
- Entity Culling culls entities on the CPU before rendering
- ThreadCraft doesn't modify entity rendering — compatible

### Starlight
- Starlight replaces the lighting engine
- ThreadCraft's `lightingPool` should defer to Starlight if present (check mod loaded)

### General Fabric Mod Compatibility
- Use Fabric API events (`FabricLifecycleEvents`, `CommonSetupEvents`)
- Mixin only where absolutely necessary (target specific methods, minimize surface area)
- No base class edits — pure Fabric + Mixin

---

## 7. Mixin Targets (Minimal)

Only use Mixin for injection points that Fabric events don't cover:

| Target | Method | Purpose |
|--------|--------|---------|
| `ChunkSource` | `getChunk(int, int, ChunkStatus, boolean)` | Intercept to return async future |
| `LevelRenderer` | `rebuildChunk()` | Intercept to submit to meshing pool |
| `Minecraft` | `tick()` | Poll pending chunk futures on main thread |

---

## 8. Build Configuration

**Gradle (`build.gradle`):**
```groovy
plugins {
    id 'fabric-loom' version '1.6-SNAPSHOT'
    id 'maven-publish'
}

dependencies {
    minecraft "com.mojang:minecraft:26.2"
    mappings "net.fabricmc:yarn:26.2+build.1:v2"
    modImplementation "net.fabricmc:fabric-loader:0.17.+"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.100.+"
    implementation "com.github.terraformers:cloth-config:15.0.+" // optional config GUI
}

processResources {
    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}
```

**fabric.mod.json:**
```json
{
  "schemaVersion": 1,
  "id": "threadcraft",
  "version": "${version}",
  "name": "ThreadCraft",
  "description": "Multi-threaded world generation and chunk meshing for Minecraft",
  "authors": ["ThreadCraft Team"],
  "contact": {},
  "license": "MIT",
  "environment": "*",
  "entrypoints": {
    "main": ["com.threadcraft.ThreadCraftMod"],
    "client": ["com.threadcraft.client.ThreadCraftClient"]
  },
  "mixins": ["threadcraft.mixins.json"],
  "depends": {
    "fabricloader": ">=0.17.0",
    "minecraft": "~26.2",
    "java": ">=21",
    "fabric-api": "*"
  },
  "breaks": {},
  "conflicts": {},
  "custom": {}
}
```

---

## 9. Implementation Roadmap

### Phase 1 — Foundation (Week 1)
- [ ] Fabric mod project scaffold (Gradle + Loom)
- [ ] Mod initialization, logging, config registration
- [ ] `generationPool`, `meshingPool`, `lightingPool` setup
- [ ] Basic async chunk generation (submit → poll → install)
- [ ] Compile & run test

### Phase 2 — World Generation (Week 2)
- [ ] Full async chunk generation pipeline
- [ ] Lighting calculation on `lightingPool`
- [ ] Back-pressure (max pending chunks)
- [ ] Benchmark: world gen time before/after

### Phase 3 — Chunk Meshing (Week 3)
- [ ] Immutable `ChunkSnapshot` capture
- [ ] Async mesh building on `meshingPool`
- [ ] GPU upload on main thread (compatible with Sodium)
- [ ] Benchmark: FPS stability when exploring

### Phase 4 — Polish & Compatibility (Week 4)
- [ ] Config GUI (Cloth Config)
- [ ] Compatibility checks with Sodium, Lithium, Entity Culling
- [ ] Mixin surface area review (minimize)
- [ ] Bug fixes, crash reports
- [ ] Release build

---

## 10. Testing & Benchmarking

### Metrics to Track
- **World gen time:** ms per chunk (lower is better)
- **Chunk gen TPS:** chunks generated per second
- **FPS:** frames per second (Sodium + ThreadCraft vs Sodium alone)
- **Frame time:** ms per frame (stutter measurement)
- **Memory:** heap usage (ensure back-pressure prevents OOM)

### Test Scenarios
1. Fly in creative mode through new terrain (stress gen + mesh)
2. Stand still in a dense area (baseline FPS)
3. Rapid chunk unload/reload (back-pressure test)
4. With and without Sodium (compatibility verification)

---

## 11. Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Thread safety bugs (concurrent modification) | Immutable snapshots, main-thread-only installs, extensive testing |
| Performance regression on single-core CPUs | Config option to disable async features; default to 1 thread if 1 core |
| Incompatibility with future Minecraft versions | Target specific Fabric version; use Fabric API events over Mixin where possible |
| Conflicts with other async mods | Check for existing async implementations; coordinate via Fabric API events |

---

## 12. Directory Structure (Planned)

```
src/main/java/com/threadcraft/
├── ThreadCraftMod.java          # Mod entry point (common)
├── ThreadCraftClient.java       # Client entry point
├── config/ThreadCraftConfig.java # Config handling
├── executor/                    # Thread pool management
│   ├── GenerationExecutor.java
│   ├── MeshingExecutor.java
│   └── LightingExecutor.java
├── worldgen/                    # Async chunk generation
│   ├── AsyncChunkGenerator.java
│   └── ChunkGenerationTask.java
├── mesh/                        # Async chunk meshing
│   ├── AsyncMesher.java
│   └── ChunkSnapshot.java       # Immutable chunk data copy
├── mixin/                       # Mixin classes (minimal)
│   ├── MixinChunkSource.java
│   └── MixinLevelRenderer.java
└── events/                      # Fabric event listeners
    ├── ChunkGenerationEvents.java
    └── RenderTickEvents.java
```

---

## 13. License

MIT — open source, modpack-friendly.

---

*Plan version: 1.0*  
*Target Minecraft: 26.2 (Fabric)*  
*Compatibility: Sodium, Lithium, Entity Culling, Starlight, Fabric API*