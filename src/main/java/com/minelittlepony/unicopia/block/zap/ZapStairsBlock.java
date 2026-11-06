package com.minelittlepony.unicopia.block.zap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ZapStairsBlock extends StairBlock {

    private final BlockState baseBlock;

    public ZapStairsBlock(BlockState baseBlockState, Properties settings) {
        super(baseBlockState, settings);
        this.baseBlock = baseBlockState;
    }


    @Deprecated
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        return baseBlock.getDestroyProgress(player, world, pos);
    }
}
