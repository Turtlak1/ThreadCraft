package org.turtlak.threadcraft.mixin.client;

import org.turtlak.threadcraft.client.config.ThreadCraftClothConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {

    @Shadow
    private int width;
    @Shadow
    private int height;
    @Shadow
    protected Minecraft minecraft;
    @Shadow
    protected abstract Button addRenderableWidget(Button button);

    @Inject(method = "init", at = @At("TAIL"))
    private void onOptionsScreenInit(CallbackInfo ci) {
        addRenderableWidget(Button.builder(Component.literal("Threadcraft"), btn -> {
            minecraft.setScreenAndShow(ThreadCraftClothConfig.createScreen((Screen) (Object) this));
        }).bounds(width / 2 - 100, height - 40, 200, 20).build());
    }
}
