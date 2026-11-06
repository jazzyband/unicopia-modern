package com.minelittlepony.unicopia.server.world.gen;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import org.joml.Vector2i;

import com.minelittlepony.unicopia.block.FruitBearingBlock;
import com.minelittlepony.unicopia.server.world.UWorldGen;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class FruitBlobFoliagePlacer extends BlobFoliagePlacer {
    public static final MapCodec<FruitBlobFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> blobParts(instance).apply(instance, FruitBlobFoliagePlacer::new));

    public FruitBlobFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset, height);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return UWorldGen.FRUIT_BLOB_FOLIAGE_PLACER_TYPE;
    }

    @Override
    protected void createFoliage(WorldGenLevel world, FoliageSetter placer, RandomSource random, TreeFeature config, int trunkHeight, FoliageAttachment treeNode, int foliageHeight, int radius, int offset) {
        final Map<Vector2i, Pair<FruitBearingBlock, Integer>> leafPositions = new HashMap<>();
        super.createFoliage(world, new FoliageSetter() {
            @Override
            public void set(BlockPos pos, BlockState state) {
                placer.set(pos, state);
                if (state.getBlock() instanceof FruitBearingBlock block
                        && block.isPositionValidForFruit(state, pos)) {
                    leafPositions.compute(new Vector2i(pos.getX(), pos.getZ()), (col, original) -> original == null || original.getSecond() > pos.getY() ? Pair.of(block, pos.getY()) : original);
                }
            }

            @Override
            public boolean isSet(BlockPos pos) {
                return placer.isSet(pos);
            }
        }, random, config, trunkHeight, treeNode, foliageHeight, radius, offset);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        leafPositions.entrySet().forEach(pos -> {
            mutable.set(pos.getKey().x(), pos.getValue().getSecond() - 1, pos.getKey().y());
            if (!placer.isSet(mutable) && random.nextInt(12) == 0) {
                placer.set(mutable, pos.getValue().getFirst().getPlacedFruitState(random));
            }
        });
    }
}
