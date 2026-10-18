package org.turtlak.threadcraft.client;

import org.turtlak.threadcraft.client.config.ThreadCraftClothConfig;
import org.turtlak.threadcraft.ThreadCraftMod;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadCraftClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("threadcraft");

    @Override
    public void onInitializeClient() {
        registerModMenuConfigScreen();
        LOGGER.info("ThreadCraft client initialized");
    }

    private static void registerModMenuConfigScreen() {
        try {
            Class<?> modMenuApi = Class.forName("me.shedaniel.modmenu.api.ModMenuApi");
            Class<?> factoryClass = Class.forName("me.shedaniel.modmenu.api.ConfigScreenFactory");
            java.lang.reflect.Method register = modMenuApi.getMethod(
                "registerModMenuConfigScreen", String.class, factoryClass
            );
            Object factory = java.lang.reflect.Proxy.newProxyInstance(
                ThreadCraftClient.class.getClassLoader(),
                new Class<?>[]{factoryClass},
                (proxy, method, args) -> ThreadCraftClothConfig.createScreen((Screen) args[0])
            );
            register.invoke(null, ThreadCraftMod.MOD_ID, factory);
            LOGGER.info("ThreadCraft config screen registered with Mod Menu");
        } catch (ClassNotFoundException e) {
            LOGGER.debug("Mod Menu not installed, skipping config screen registration");
        } catch (Exception e) {
            LOGGER.warn("Failed to register ThreadCraft config screen with Mod Menu", e);
        }
    }
}
