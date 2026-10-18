package org.turtlak.threadcraft.mixin;

import org.turtlak.threadcraft.executor.ThreadCraftExecutors;
import net.minecraft.world.level.chunk.ChunkSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSource.class)
public class MixinChunkSource {

    @Inject(method = "getChunk", at = @At("HEAD"))
    private void onGetChunk(CallbackInfoReturnable<?> cir) {
        if (ThreadCraftExecutors.generationEnabled()) {
            ThreadCraftExecutors.submitGeneration(() -> {
            });
        }
    }
}
