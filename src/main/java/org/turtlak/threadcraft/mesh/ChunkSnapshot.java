package org.turtlak.threadcraft.mesh;

import java.util.Map;

/**
 * Immutable snapshot of chunk data captured on the main thread and handed to
 * worker threads for off-thread mesh building.
 */
public record ChunkSnapshot(
        int chunkX,
        int chunkZ,
        byte[] blockStates,
        byte[] blockLight,
        byte[] skyLight,
        Map<String, Object> extraData
) {
}
