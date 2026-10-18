package org.turtlak.threadcraft.events;

import org.turtlak.threadcraft.executor.ThreadCraftExecutors;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class RenderTickEvents {

    private RenderTickEvents() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ThreadCraftExecutors.pollMeshing());
    }
}
