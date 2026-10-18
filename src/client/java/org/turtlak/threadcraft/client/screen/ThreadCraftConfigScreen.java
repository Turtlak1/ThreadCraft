package org.turtlak.threadcraft.client.screen;

import org.turtlak.threadcraft.config.ThreadCraftConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ThreadCraftConfigScreen extends Screen {

    private final Screen parent;

    public ThreadCraftConfigScreen(Screen parent) {
        super(Component.literal("ThreadCraft Settings"));
        this.parent = parent;
    }

    @Override
    public void init() {
        int y = 40;

        y = addBoolRow(y, "Async Generation", ThreadCraftConfig::enableAsyncGen,
            v -> ThreadCraftConfig.enableAsyncGen = v);
        y = addBoolRow(y, "Async Meshing", ThreadCraftConfig::enableAsyncMesh,
            v -> ThreadCraftConfig.enableAsyncMesh = v);
        y = addIntRow(y, "Gen Threads", ThreadCraftConfig::generationThreads,
            v -> ThreadCraftConfig.generationThreads = v,
            1, Runtime.getRuntime().availableProcessors());
        y = addIntRow(y, "Mesh Threads", ThreadCraftConfig::meshingThreads,
            v -> ThreadCraftConfig.meshingThreads = v,
            1, Runtime.getRuntime().availableProcessors());
        y = addIntRow(y, "Lighting Threads", ThreadCraftConfig::lightingThreads,
            v -> ThreadCraftConfig.lightingThreads = v,
            1, Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
        y = addIntRow(y, "Max Pending", ThreadCraftConfig::maxPendingChunks,
            v -> ThreadCraftConfig.maxPendingChunks = v,
            4, 128);

        addRenderableWidget(Button.builder(Component.literal("Save"), btn -> {
            ThreadCraftConfig.save();
            minecraft.setScreenAndShow(parent);
        }).bounds(width / 2 - 100, height - 40, 200, 20).build());
    }

    private int addBoolRow(int y, String label, java.util.function.BooleanSupplier getter,
                           java.util.function.Consumer<Boolean> setter) {
        Button btn = Button.builder(Component.literal(label + ": " + (getter.getAsBoolean() ? "ON" : "OFF")), b -> {
            boolean next = !getter.getAsBoolean();
            setter.accept(next);
            ThreadCraftConfig.save();
            b.setMessage(Component.literal(label + ": " + (next ? "ON" : "OFF")));
        }).bounds(width / 2 - 100, y, 200, 20).build();
        addRenderableWidget(btn);
        return y + 24;
    }

    private int addIntRow(int y, String label, java.util.function.IntSupplier getter,
                          java.util.function.IntConsumer setter, int min, int max) {
        Button[] btns = new Button[3];
        btns[0] = Button.builder(Component.literal("-"), b -> {
            int next = Math.max(min, getter.getAsInt() - 1);
            setter.accept(next);
            ThreadCraftConfig.save();
            btns[1].setMessage(Component.literal(String.valueOf(next)));
        }).bounds(width / 2 - 120, y, 20, 20).build();
        btns[1] = Button.builder(Component.literal(String.valueOf(getter.getAsInt())), b -> {
            int next = Math.min(max, getter.getAsInt() + 1);
            setter.accept(next);
            ThreadCraftConfig.save();
            btns[1].setMessage(Component.literal(String.valueOf(next)));
        }).bounds(width / 2 - 100, y, 100, 20).build();
        btns[2] = Button.builder(Component.literal("+"), b -> {
            int next = Math.min(max, getter.getAsInt() + 1);
            setter.accept(next);
            ThreadCraftConfig.save();
            btns[1].setMessage(Component.literal(String.valueOf(next)));
        }).bounds(width / 2 + 100, y, 20, 20).build();
        addRenderableWidget(btns[0]);
        addRenderableWidget(btns[1]);
        addRenderableWidget(btns[2]);
        return y + 24;
    }

    @Override
    public void onClose() {
        ThreadCraftConfig.save();
        minecraft.setScreenAndShow(parent);
    }
}
