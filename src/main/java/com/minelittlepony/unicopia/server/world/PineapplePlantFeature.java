package com.minelittlepony.unicopia.server.world;

import com.minelittlepony.unicopia.block.PineappleCropBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class PineapplePlantFeature implements Feature {

    public static final MapCodec<PineapplePlantFeature> CODEC = MapCodec.unit(new PineapplePlantFeature());

    public PineapplePlantFeature() {
    }

    @Override
    public MapCodec<PineapplePlantFeature> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator generator, RandomSource random, BlockPos origin) {

        int xOffset = 3;
        int zOffset = 3;

        BlockPos.MutableBlockPos mutablePos = origin.mutable();
        boolean succeeded = false;
        for (BlockPos position : BlockPos.betweenClosed(origin.offset(-xOffset, 0, -zOffset), origin.offset(xOffset, 0, zOffset))) {
            if (random.nextFloat() < 0.5) {
                continue;
            }

            findTerrainLevel(world, mutablePos.set(position));

            if (world.isOutsideBuildHeight(mutablePos)
                    || (!world.isEmptyBlock(mutablePos) && !isReplaceable(world, mutablePos))
                    || !world.getBlockState(mutablePos.move(Direction.DOWN)).is(BlockTags.DIRT)) {
                continue;
            }

            mutablePos.move(Direction.UP);

            int maxAge = UBlocks.PINEAPPLE.getMaxAge();
            int age = random.nextIntBetweenInclusive(maxAge - 3, maxAge);

            succeeded = true;
            setBlock(world, mutablePos, UBlocks.PINEAPPLE.getStateForAge(age).setValue(PineappleCropBlock.WILD, true));

            if (age >= maxAge) {
                mutablePos.move(Direction.UP);

                if (isReplaceable(world, mutablePos)) {
                    setBlock(world, mutablePos, UBlocks.PINEAPPLE
                            .getStateForAge(random.nextIntBetweenInclusive(maxAge / 2, maxAge))
                            .setValue(PineappleCropBlock.HALF, Half.TOP)
                            .setValue(PineappleCropBlock.WILD, true));
                }
            }
        }

        return succeeded;
    }

    static void findTerrainLevel(WorldGenLevel world, BlockPos.MutableBlockPos mutablePos) {
        if (isReplaceable(world, mutablePos)) {
            do {
                mutablePos.move(Direction.DOWN);
            } while (isReplaceable(world, mutablePos) && !world.isOutsideBuildHeight(mutablePos));

            mutablePos.move(Direction.UP);
        }

        if (!isReplaceable(world, mutablePos)) {
            do {
                mutablePos.move(Direction.UP);
            } while (!isReplaceable(world, mutablePos) && !world.isOutsideBuildHeight(mutablePos));
        }
    }

    static boolean isReplaceable(WorldGenLevel world, BlockPos pos) {
        return (world.isEmptyBlock(pos) || world.getBlockState(pos).is(BlockTags.REPLACEABLE_BY_TREES)) && world.getBlockState(pos).getFluidState().isEmpty();
    }

}
