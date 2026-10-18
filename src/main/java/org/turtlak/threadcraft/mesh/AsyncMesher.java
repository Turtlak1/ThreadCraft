package org.turtlak.threadcraft.mesh;

import org.turtlak.threadcraft.executor.ThreadCraftExecutors;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class AsyncMesher {

    public static <T> CompletableFuture<T> submit(Supplier<T> meshBuilder) {
        return CompletableFuture.supplyAsync(meshBuilder, ThreadCraftExecutors.meshingPool());
    }
}
