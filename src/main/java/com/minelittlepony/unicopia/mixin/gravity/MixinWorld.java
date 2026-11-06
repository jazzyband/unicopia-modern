package com.minelittlepony.unicopia.mixin.gravity;

import java.util.Stack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.duck.RotatedView;

@Mixin(Level.class)
abstract class MixinWorld implements LevelAccessor, RotatedView {

    private int recurseCount = 0;
    private final Stack<Integer> rotations = new Stack<>();

    @Override
    public Stack<Integer> getRotations() {
        return rotations;
    }

    @Override
    public boolean hasTransform() {
        return recurseCount <= 0;
    }

    @ModifyVariable(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"))
    private BlockPos modifyBlockPos(BlockPos pos) {
        pos = applyRotation(pos);
        recurseCount = Math.max(0, recurseCount) + 1;
        return pos;
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("RETURN"))
    public void onSetBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> info) {
        recurseCount = Math.max(0, recurseCount - 1);
    }
}
