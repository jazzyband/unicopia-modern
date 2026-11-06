package com.minelittlepony.unicopia.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface CropBlockAccessor {
    float unicopia_getAvailableMoisture(BlockState state, BlockGetter world, BlockPos pos);
}
