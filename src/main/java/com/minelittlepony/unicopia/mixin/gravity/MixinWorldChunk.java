package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.entity.duck.RotatedView;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(LevelChunk.class)
abstract class MixinWorldChunk {

    @ModifyVariable(method = {
            "getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/world/level/block/state/BlockState;",
            "getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
            "removeBlockEntity(Lnet/minecraft/core/BlockPos;)V",
    }, at = @At("HEAD"))
    private BlockPos modifyBlockPos(BlockPos pos) {
        return ((RotatedView)((LevelChunk)(Object)this).getLevel()).applyRotation(pos);
    }

    @ModifyVariable(method = "getFluidState(III)Lnet/minecraft/world/level/material/FluidState;", at = @At("HEAD"), ordinal = 1)
    private int modifyFluidPos(int y) {
        return ((RotatedView)((LevelChunk)(Object)this).getLevel()).applyRotation(y);
    }
}
