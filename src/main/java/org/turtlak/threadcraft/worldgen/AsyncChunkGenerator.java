package org.turtlak.threadcraft.worldgen;

import org.turtlak.threadcraft.executor.ThreadCraftExecutors;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class AsyncChunkGenerator {

    public static <T> CompletableFuture<T> submit(Supplier<T> generator) {
        return CompletableFuture.supplyAsync(generator, ThreadCraftExecutors.generationPool());
    }
}
