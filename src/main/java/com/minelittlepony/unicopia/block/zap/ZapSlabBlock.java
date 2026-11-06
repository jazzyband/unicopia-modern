package com.minelittlepony.unicopia.block.zap;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ZapSlabBlock extends SlabBlock implements ElectrifiedBlock {

    public ZapSlabBlock(Properties settings) {
        super(settings);
    }


    @Deprecated
    @Override
    public void attack(BlockState state, Level world, BlockPos pos, Player player) {
        triggerLightning(state, world, pos);
    }

    @Deprecated
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        return getBlockBreakingDelta(super.getDestroyProgress(state, player, world, pos), player);
    }
}
