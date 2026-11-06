package com.minelittlepony.unicopia.mixin.forgified;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.minelittlepony.unicopia.block.CropBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CropBlock.class)
public class MixinCropBlock implements CropBlockAccessor {
    @Override
    public float unicopia_getAvailableMoisture(BlockState state, BlockGetter world, BlockPos pos) {
        return getAvailableMoisture(state, world, pos);
    }

    @Shadow(aliases = { "getAvailableMoisture", "getGrowthSpeed" })
    private static float getAvailableMoisture(BlockState state, BlockGetter world, BlockPos pos) {
        return 0;
    }
}
