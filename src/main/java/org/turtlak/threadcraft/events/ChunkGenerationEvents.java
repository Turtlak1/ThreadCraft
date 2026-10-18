package org.turtlak.threadcraft.events;

import org.turtlak.threadcraft.executor.ThreadCraftExecutors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public final class ChunkGenerationEvents {

    private ChunkGenerationEvents() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> ThreadCraftExecutors.pollGeneration());
    }
}
