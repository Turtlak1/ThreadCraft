package org.turtlak.threadcraft.worldgen;

public record ChunkGenerationTask(int chunkX, int chunkZ, long worldSeed) implements Runnable {

    @Override
    public void run() {
        // Chunk generation stages (noise, biomes, features, structures) run off-thread.
        // The result is installed into the world on the main thread by the caller.
    }
}
