package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.block.cloud.CloudLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

@Mixin(Heightmap.class)
abstract class MixinHeightmap {
    @Inject(method = "lambda$static$0", at = @At("HEAD"), cancellable = true)
    private static void excludeCloudsFromWorldSurfaceHeightMap(BlockState state, CallbackInfoReturnable<Boolean> info) {
        if (state.getBlock() instanceof CloudLike) {
            info.setReturnValue(false);
        }
    }
}
