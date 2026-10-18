package org.turtlak.threadcraft.mixin.client;

import org.turtlak.threadcraft.executor.ThreadCraftExecutors;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Inject(method = "rebuildChunk", at = @At("HEAD"))
    private void onRebuildChunk(CallbackInfo ci) {
        if (ThreadCraftExecutors.meshingEnabled()) {
            ThreadCraftExecutors.submitMeshing(() -> {
            });
        }
    }
}
