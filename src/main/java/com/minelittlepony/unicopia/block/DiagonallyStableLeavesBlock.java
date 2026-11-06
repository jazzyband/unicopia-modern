package com.minelittlepony.unicopia.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Leaves block that checks the diagonals for whether they can decay.
 */
public class DiagonallyStableLeavesBlock extends TintedParticleLeavesBlock {
    private static final int MIN_DISTANCE = 1;

    public DiagonallyStableLeavesBlock(float leafParticleChance, Properties settings) {
        super(leafParticleChance, settings);
    }


    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        world.setBlock(pos, state.setValue(DISTANCE, getDistanceFromLogs(world, pos)), Block.UPDATE_ALL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
            .setValue(PERSISTENT, true)
            .setValue(WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).is(Fluids.WATER))
            .setValue(DISTANCE, getDistanceFromLogs(ctx.getLevel(), ctx.getClickedPos()));
    }

    private static int getDistanceFromLogs(LevelAccessor world, BlockPos pos) {
        int distance = DECAY_DISTANCE;

        distance = Math.min(distance, getOptionalDistanceAt(world.getBlockState(pos.above())).orElse(DECAY_DISTANCE) + 1);
        if (distance == MIN_DISTANCE) {
            return distance;
        }
        distance = Math.min(distance, getOptionalDistanceAt(world.getBlockState(pos.below())).orElse(DECAY_DISTANCE) + 1);
        if (distance == MIN_DISTANCE) {
            return distance;
        }
        for (BlockPos i : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
            if (i.equals(pos)) {
                continue;
            }
            int increment = i.getX() == pos.getX() || i.getZ() == pos.getZ() ? 1 : 2;
            distance = Math.min(distance, getOptionalDistanceAt(world.getBlockState(i)).orElse(DECAY_DISTANCE) + increment);
            if (distance == MIN_DISTANCE) {
                return distance;
            }
        }

        return distance;
    }
}
