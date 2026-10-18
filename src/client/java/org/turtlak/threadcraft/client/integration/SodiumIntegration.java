package org.turtlak.threadcraft.client.integration;

import org.turtlak.threadcraft.client.screen.ThreadCraftConfigScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SodiumIntegration {

    private static final Logger LOGGER = LoggerFactory.getLogger("threadcraft");

    private static final String SODIUM_OPTIONS_SCREEN = "me.jellysquid.mods.sodium.client.gui.options.SodiumOptionsScreen";

    private SodiumIntegration() {
    }

    public static void init() {
        if (!FabricLoader.getInstance().isModLoaded("sodium")) {
            return;
        }
        try {
            Class.forName(SODIUM_OPTIONS_SCREEN);
            LOGGER.info("Sodium detected alongside ThreadCraft");
        } catch (ClassNotFoundException e) {
            LOGGER.debug("Sodium options screen class not found at expected path");
        }
    }

    public static Screen wrapOrOpen(Screen current) {
        return new ThreadCraftConfigScreen(current);
    }
}
