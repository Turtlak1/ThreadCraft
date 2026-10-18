package org.turtlak.threadcraft.client.config;

import org.turtlak.threadcraft.config.ThreadCraftConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ThreadCraftClothConfig {

    public static Screen createScreen(Screen parent) {
        ConfigEntryBuilder entries = ConfigEntryBuilder.create();
        ConfigBuilder builder = ConfigBuilder.create()
            .setTitle(Component.literal("ThreadCraft Settings"))
            .setSavingRunnable(ThreadCraftConfig::save);

        ConfigCategory main = builder.getOrCreateCategory(Component.literal("ThreadCraft"));

        main.addEntry(entries.startBooleanToggle(
            Component.literal("Async Generation"), ThreadCraftConfig.enableAsyncGen
        ).setSaveConsumer(v -> ThreadCraftConfig.enableAsyncGen = v).build());

        main.addEntry(entries.startBooleanToggle(
            Component.literal("Async Meshing"), ThreadCraftConfig.enableAsyncMesh
        ).setSaveConsumer(v -> ThreadCraftConfig.enableAsyncMesh = v).build());

        main.addEntry(entries.startIntSlider(
            Component.literal("Generation Threads"),
            1, Runtime.getRuntime().availableProcessors(),
            ThreadCraftConfig.generationThreads
        ).setSaveConsumer(v -> ThreadCraftConfig.generationThreads = v).build());

        main.addEntry(entries.startIntSlider(
            Component.literal("Meshing Threads"),
            1, Runtime.getRuntime().availableProcessors(),
            ThreadCraftConfig.meshingThreads
        ).setSaveConsumer(v -> ThreadCraftConfig.meshingThreads = v).build());

        main.addEntry(entries.startIntSlider(
            Component.literal("Lighting Threads"),
            1, Math.max(1, Runtime.getRuntime().availableProcessors() / 2),
            ThreadCraftConfig.lightingThreads
        ).setSaveConsumer(v -> ThreadCraftConfig.lightingThreads = v).build());

        main.addEntry(entries.startIntSlider(
            Component.literal("Max Pending Chunks"),
            4, 128, ThreadCraftConfig.maxPendingChunks
        ).setSaveConsumer(v -> ThreadCraftConfig.maxPendingChunks = v).build());

        return builder.setParentScreen(parent).build();
    }
}
