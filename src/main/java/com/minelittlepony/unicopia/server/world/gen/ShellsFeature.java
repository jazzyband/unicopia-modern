package com.minelittlepony.unicopia.server.world.gen;

import com.minelittlepony.unicopia.block.ShellsBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

public final class ShellsFeature implements Feature {
    public static final MapCodec<ShellsFeature> CODEC = MapCodec.unit(new ShellsFeature());

    @Override
    public MapCodec<ShellsFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        var states = new RandomizedIntStateProvider(new WeightedStateProvider(WeightedList.<BlockState>builder()
                .add(UBlocks.CLAM_SHELL.defaultBlockState(), 1)
                .add(UBlocks.SCALLOP_SHELL.defaultBlockState(), 2)
                .add(UBlocks.TURRET_SHELL.defaultBlockState(), 2)), ShellsBlock.COUNT, UniformInt.of(1, 4));
        boolean placed = false;
        for (int attempt = 0; attempt < 25; attempt++) {
            BlockPos pos = origin.offset(random.nextIntBetweenInclusive(-3, 3), random.nextIntBetweenInclusive(-1, 1), random.nextIntBetweenInclusive(-3, 3));
            if (world.getBlockState(pos).is(Blocks.WATER)
                    && world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP)) {
                BlockState state = states.getState(world, random, pos);
                if (state.canSurvive(world, pos)) {
                    world.setBlock(pos, state, 3);
                    placed = true;
                }
            }
        }
        return placed;
    }
}
