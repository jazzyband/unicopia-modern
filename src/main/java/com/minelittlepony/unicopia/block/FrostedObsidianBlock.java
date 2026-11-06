package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.Untyped;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FrostedObsidianBlock extends FrostedIceBlock {

    public FrostedObsidianBlock(Properties settings) {
        super(settings);
    }


    @Override
    public void playerDestroy(net.minecraft.server.level.ServerLevel world, net.minecraft.server.level.ServerPlayer player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005f);
        Block.dropResources(state, world, pos, blockEntity, player, stack);
    }

    @Override
    protected void melt(BlockState state, Level world, BlockPos pos) {
        if (world.dimension().equals(net.minecraft.world.level.Level.NETHER)) {
            world.removeBlock(pos, false);
            return;
        }
        world.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
        world.neighborChanged(pos, Blocks.LAVA, null);
    }
}
