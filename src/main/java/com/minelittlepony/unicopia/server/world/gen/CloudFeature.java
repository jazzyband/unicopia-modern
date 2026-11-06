package com.minelittlepony.unicopia.server.world.gen;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Generates clusters of sky clouds, including the damp exposed surface. */
public final class CloudFeature implements Feature {
    public static final MapCodec<CloudFeature> CODEC = MapCodec.unit(new CloudFeature());

    @Override
    public MapCodec<CloudFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Unicopia.getConfig().enableCloudGen.get()) {
            return false;
        }
        boolean placed = false;
        int bubbles = 10 + random.nextInt(12);
        for (int bubble = 0; bubble < bubbles; bubble++) {
            BlockPos center = origin.offset(random.nextIntBetweenInclusive(-12, 12), random.nextIntBetweenInclusive(-5, 5), random.nextIntBetweenInclusive(-12, 12));
            double radius = 2.5 + random.nextDouble() * 4;
            double height = 1.5 + random.nextDouble() * 2.5;
            int horizontal = (int)Math.ceil(radius);
            int vertical = (int)Math.ceil(height);
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-horizontal, -vertical, -horizontal), center.offset(horizontal, vertical, horizontal))) {
                if (!withinGenerationRadius(origin, pos)) {
                    continue;
                }
                double dx = (pos.getX() - center.getX()) / radius;
                double dy = (pos.getY() - center.getY()) / height;
                double dz = (pos.getZ() - center.getZ()) / radius;
                if (dx * dx + dy * dy + dz * dz <= 1 && world.isEmptyBlock(pos)) {
                    world.setBlock(pos, UBlocks.CLOUD.defaultBlockState(), 2);
                    placed = true;
                }
            }
        }
        if (placed) {
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-20, -9, -20), origin.offset(20, 9, 20))) {
                if (!withinGenerationRadius(origin, pos)) {
                    continue;
                }
                if (world.getBlockState(pos).is(UBlocks.CLOUD) && world.isEmptyBlock(pos.above())) {
                    world.setBlock(pos, UBlocks.SOGGY_CLOUD.defaultBlockState(), 2);
                }
            }
        }
        return placed;
    }

    private static boolean withinGenerationRadius(BlockPos origin, BlockPos pos) {
        return Math.abs((pos.getX() >> 4) - (origin.getX() >> 4)) <= 1
                && Math.abs((pos.getZ() >> 4) - (origin.getZ() >> 4)) <= 1;
    }
}
