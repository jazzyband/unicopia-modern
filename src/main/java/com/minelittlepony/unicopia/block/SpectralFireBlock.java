package com.minelittlepony.unicopia.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SpectralFireBlock extends SoulFireBlock {

    public SpectralFireBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean isMoving) {
        if (!(entity instanceof ItemEntity)) {
            super.entityInside(state, world, pos, entity, handler, isMoving);
        }
    }
}
