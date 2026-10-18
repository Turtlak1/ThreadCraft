package org.turtlak.threadcraft.executor;

import org.turtlak.threadcraft.config.ThreadCraftConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadCraftExecutors {

    private static final Logger LOGGER = LoggerFactory.getLogger("threadcraft");

    private static ThreadPoolExecutor generationPool;
    private static ThreadPoolExecutor meshingPool;
    private static ThreadPoolExecutor lightingPool;

    private static final List<CompletableFuture<?>> pendingGeneration = new ArrayList<>();
    private static final List<CompletableFuture<?>> pendingMeshing = new ArrayList<>();

    private static boolean initialized = false;

    public static synchronized void init(int genThreads, int meshThreads, int lightThreads, int maxPending) {
        if (initialized) {
            shutdown();
        }
        generationPool = newPool("threadcraft-gen", genThreads, maxPending);
        meshingPool = newPool("threadcraft-mesh", meshThreads, maxPending);
        lightingPool = newPool("threadcraft-light", lightThreads, maxPending);
        initialized = true;
        LOGGER.info("ThreadCraft pools: gen={} mesh={} light={} maxPending={}", genThreads, meshThreads, lightThreads, maxPending);
    }

    public static synchronized void clientInit() {
    }

    private static ThreadPoolExecutor newPool(String namePrefix, int threads, int maxPending) {
        int queueCap = Math.max(1, maxPending);
        var queue = new ArrayBlockingQueue<Runnable>(queueCap);
        java.util.concurrent.ThreadFactory factory = (Runnable r) -> {
            Thread t = new Thread(r, namePrefix + "-" + poolCounter.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
        return new ThreadPoolExecutor(
            threads, threads,
            0L, TimeUnit.MILLISECONDS,
            queue, factory,
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

    private static final java.util.concurrent.atomic.AtomicInteger poolCounter = new java.util.concurrent.atomic.AtomicInteger(0);

    public static boolean submitGeneration(Runnable task) {
        return submit(generationPool, task, ThreadCraftConfig.enableAsyncGen);
    }

    public static boolean submitMeshing(Runnable task) {
        return submit(meshingPool, task, ThreadCraftConfig.enableAsyncMesh);
    }

    public static boolean submitLighting(Runnable task) {
        return submit(lightingPool, task, true);
    }

    private static boolean submit(ThreadPoolExecutor pool, Runnable task, boolean enabled) {
        if (pool == null || !enabled) {
            return false;
        }
        try {
            pool.execute(task);
            return true;
        } catch (RejectedExecutionException e) {
            return false;
        }
    }

    public static void registerGenerationFuture(CompletableFuture<?> future, Runnable onComplete) {
        pendingGeneration.add(future.whenCompleteAsync((r, e) -> onComplete.run()));
    }

    public static void registerMeshingFuture(CompletableFuture<?> future, Runnable onComplete) {
        pendingMeshing.add(future.whenCompleteAsync((r, e) -> onComplete.run()));
    }

    public static void pollGeneration() {
        pendingGeneration.removeIf(CompletableFuture::isDone);
    }

    public static void pollMeshing() {
        pendingMeshing.removeIf(CompletableFuture::isDone);
    }

    public static boolean generationEnabled() {
        return ThreadCraftConfig.enableAsyncGen;
    }

    public static boolean meshingEnabled() {
        return ThreadCraftConfig.enableAsyncMesh;
    }

    public static void shutdown() {
        if (generationPool != null) generationPool.shutdownNow();
        if (meshingPool != null) meshingPool.shutdownNow();
        if (lightingPool != null) lightingPool.shutdownNow();
        try {
            if (generationPool != null) generationPool.awaitTermination(5, TimeUnit.SECONDS);
            if (meshingPool != null) meshingPool.awaitTermination(5, TimeUnit.SECONDS);
            if (lightingPool != null) lightingPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pendingGeneration.clear();
        pendingMeshing.clear();
        initialized = false;
    }

    public static ThreadPoolExecutor generationPool() { return generationPool; }
    public static ThreadPoolExecutor meshingPool() { return meshingPool; }
    public static ThreadPoolExecutor lightingPool() { return lightingPool; }
}
