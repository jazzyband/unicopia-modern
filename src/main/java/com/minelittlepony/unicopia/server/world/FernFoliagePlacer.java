package com.minelittlepony.unicopia.server.world;

import com.minelittlepony.unicopia.block.UBlocks;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class FernFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<FernFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> foliagePlacerParts(instance).apply(instance, FernFoliagePlacer::new));

    public FernFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return UWorldGen.FERN_FOLIAGE_PLACER_TYPE;
    }

    @Override
    protected void createFoliage(WorldGenLevel world, FoliageSetter placer, RandomSource random,
            TreeFeature config, int trunkHeight, FoliageAttachment node, int foliageHeight, int radius, int offset) {

        BlockPos center = node.pos();

        // central leaves blob
        for (int y = offset; y >= offset - foliageHeight; --y) {
            int rad = Math.max(radius + node.radiusOffsetXZ() - 3 - y / 2, 0);
            placeLeavesRow(world, placer, random, config, center, rad, y, node.doubleTrunk());
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int fanY = 0;
        for (int outset = 1; outset < 6; outset++) {
            for (int j = 0; j < 2; j++) {
                if (outset < 4) {
                    // diagonal frons
                    for (int z = 0; z < 2; z++) {
                        tryPlaceLeaf(world, placer, random, config, pos.setWithOffset(center, outset, fanY, outset + z));
                        tryPlaceLeaf(world, placer, random, config, pos.setWithOffset(center, outset, fanY, -outset + z));
                        tryPlaceLeaf(world, placer, random, config, pos.setWithOffset(center, -outset + z, fanY, outset));
                        tryPlaceLeaf(world, placer, random, config, pos.setWithOffset(center, -outset + z, fanY, -outset));
                    }
                }
                // adjacent frons
                tryPlaceLeaf(world, placer, random, config, pos.setWithOffset(center, outset, fanY, 0));
                tryPlaceLeaf(world, placer, random, config, pos.setWithOffset(center, 0, fanY, outset));
                tryPlaceLeaf(world, placer, random, config, pos.setWithOffset(center, 0, fanY, -outset));
                tryPlaceLeaf(world, placer, random, config, pos.setWithOffset(center, -outset, fanY, 0));

                if (j != 0 || outset % 2 != 0) break;
                fanY--;
            }

            // fruit
            if (outset == 1) {
                BlockState fruitState = UBlocks.BANANAS.defaultBlockState();
                if (random.nextInt(5) == 0) {
                    placer.set(pos.setWithOffset(center, outset, fanY - 1, 0), fruitState);
                }
                if (random.nextInt(5) == 0) {
                    placer.set(pos.setWithOffset(center, -outset, fanY - 1, 0), fruitState);
                }
                if (random.nextInt(5) == 0) {
                    placer.set(pos.setWithOffset(center, 0, fanY - 1, outset), fruitState);
                }
                if (random.nextInt(5) == 0) {
                    placer.set(pos.setWithOffset(center, 0, fanY - 1, -outset), fruitState);
                }
            }
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int trunkHeight, TreeFeature config) {
        return 1;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int radius, boolean giantTrunk) {
        return dx == radius && dz == radius && (random.nextInt(2) == 0 || y == 0);
    }

}
