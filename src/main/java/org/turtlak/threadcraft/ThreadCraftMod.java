package org.turtlak.threadcraft;

import org.turtlak.threadcraft.config.ThreadCraftConfig;
import org.turtlak.threadcraft.events.ChunkGenerationEvents;
import org.turtlak.threadcraft.executor.ThreadCraftExecutors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadCraftMod implements ModInitializer {

    public static final String MOD_ID = "threadcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ThreadCraftConfig.init(FabricLoader.getInstance().getGameDir().resolve("config"));
        ThreadCraftConfig.load();

        ThreadCraftExecutors.init(
            ThreadCraftConfig.generationThreads(),
            ThreadCraftConfig.meshingThreads(),
            ThreadCraftConfig.lightingThreads(),
            ThreadCraftConfig.maxPendingChunks()
        );

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("ThreadCraft initializing async generation/meshing pools");
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ThreadCraftExecutors.shutdown();
            LOGGER.info("ThreadCraft shut down");
        });

        ChunkGenerationEvents.register();

        LOGGER.info("ThreadCraft initialized");
    }
}
