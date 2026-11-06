package com.minelittlepony.unicopia.datagen;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import com.google.common.collect.ImmutableList;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.server.world.FernFoliagePlacer;
import com.minelittlepony.unicopia.server.world.Tree;
import com.minelittlepony.unicopia.server.world.UTreeGen;
import com.minelittlepony.unicopia.server.world.gen.FruitBlobFoliagePlacer;
import com.minelittlepony.unicopia.server.world.gen.UFeatureConfigs;
import com.minelittlepony.unicopia.server.world.gen.UPlacedFeatures;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class UTreeFeatures {
    static void bootstrapConfiguredFeatures(BootstrapContext<Feature> registerable) {
        registerable.register(UFeatureConfigs.ZAP_APPLE_TREE, Generator.Builder.create(Unicopia.id("zap_apple_tree"), new ForkingTrunkPlacer(
                    6, 3, 1
            ), new MegaJungleFoliagePlacer(
                    ConstantInt.of(3),
                    ConstantInt.of(1),
                    4
            ))
            .configure(builder -> builder.belowTrunkProvider(BlockStateProvider.holderOf(Blocks.DIRT)))
            .log(UBlocks.ZAP_LOG)
            .leaves(UBlocks.ZAP_LEAVES)
            .dimensions(6, 0, 8)
            .build());
        registerable.register(UFeatureConfigs.GREEN_APPLE_TREE, createAppleTreeGenerator("green_apple", UBlocks.GREEN_APPLE_LEAVES, 2).build());
        registerable.register(UFeatureConfigs.SWEET_APPLE_TREE, createAppleTreeGenerator("sweet_apple", UBlocks.SWEET_APPLE_LEAVES, 3).build());
        registerable.register(UFeatureConfigs.SOUR_APPLE_TREE, createAppleTreeGenerator("sour_apple", UBlocks.SOUR_APPLE_LEAVES, 3).build());
        registerable.register(UFeatureConfigs.GOLDEN_OAK_TREE, Generator.Builder.create(Unicopia.id("golden_oak_tree"),
                new StraightTrunkPlacer(6, 1, 3),
                new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), 3)
            ).configure(builder -> builder.belowTrunkProvider(BlockStateProvider.holderOf(Blocks.DIRT)))
            .dimensions(1, 3, 5)
            .log(UBlocks.GOLDEN_OAK_LOG)
            .leaves(UBlocks.GOLDEN_OAK_LEAVES).build());
        registerable.register(UFeatureConfigs.BANANA_TREE, Generator.Builder.create(Unicopia.id("banana_tree"),
                new StraightTrunkPlacer(4, 5, 3),
                new FernFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0))
            ).dimensions(6, 0, 8)
            .log(UBlocks.PALM_LOG)
            .leaves(UBlocks.PALM_LEAVES)
            .configure(builder -> builder.belowTrunkProvider(BlockStateProvider.holderOf(Blocks.SAND)))
            .build());
        registerable.register(UFeatureConfigs.MANGO_TREE, Generator.Builder.create(Unicopia.id("mango_tree"),
                new StraightTrunkPlacer(4, 7, 3),
                new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), 3)
            ).dimensions(9, 0, 4)
            .log(Blocks.JUNGLE_LOG)
            .leaves(UBlocks.MANGO_LEAVES)
            .configure(builder -> builder.decorators(ImmutableList.of(TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.25f)))).build());
    }

    static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> registerable) {
        var configuredFeatures = registerable.lookup(Registries.FEATURE);
        registerable.register(UPlacedFeatures.GREEN_APPLE_TREE, placement(configuredFeatures, UTreeGen.GREEN_APPLE_TREE, 0, 0.2F, 4));
        registerable.register(UPlacedFeatures.SWEET_APPLE_TREE, placement(configuredFeatures, UTreeGen.SWEET_APPLE_TREE, 0, 0.1F, 4));
        registerable.register(UPlacedFeatures.SWEET_APPLE_TREE_ORCHARD, placement(configuredFeatures, UTreeGen.SWEET_APPLE_TREE, 6, 0.1F, 3));
        registerable.register(UPlacedFeatures.SOUR_APPLE_TREE, placement(configuredFeatures, UTreeGen.SOUR_APPLE_TREE, 0, 0.2F, 4));
        registerable.register(UPlacedFeatures.ZAP_APPLE_TREE, placement(configuredFeatures, UTreeGen.ZAP_APPLE_TREE, 0, 0.01F, 1));
        registerable.register(UPlacedFeatures.BANANA_TREE, placement(configuredFeatures, UTreeGen.BANANA_TREE, 2, 0.01F, 1));
        registerable.register(UPlacedFeatures.MANGO_TREE, placement(configuredFeatures, UTreeGen.MANGO_TREE, 1, 1, 2));
    }

    private static PlacedFeature placement(HolderGetter<Feature> configuredFeatures, Tree tree, int count, float extraChance, int extraCount) {
        return new PlacedFeature(configuredFeatures.getOrThrow(tree.feature()),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(count, extraChance, extraCount), tree.sapling().orElse(Blocks.OAK_SAPLING))
        );
    }

    static Generator.Builder createAppleTreeGenerator(String name, Block leaves, int preferredDensity) {
        return Generator.Builder.create(
                    Unicopia.id(name + "_tree"),
                    new StraightTrunkPlacer(4, 3, 2),
                    new FruitBlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), 3)
            ).configure(builder -> builder.belowTrunkProvider(BlockStateProvider.holderOf(Blocks.DIRT)))
            .dimensions(1, preferredDensity - 2, preferredDensity)
            .log(Blocks.OAK_LOG)
            .leaves(leaves);
    }

    public record Generator(
            TreeFeature.Builder config,
            ResourceKey<Feature> configuredFeatureId,
            Set<Placement> placements
    ) {
        public static class Builder {
            public static Builder create(Identifier id, TrunkPlacer trunkPlacer, FoliagePlacer foliagePlacer) {
                return new Builder(trunkPlacer, foliagePlacer);
            }

            private Block logType = Blocks.OAK_LOG;
            private Block leavesType = Blocks.OAK_LEAVES;

            private final TrunkPlacer trunkPlacer;
            private final FoliagePlacer foliagePlacer;

            private Function<TreeFeature.Builder, TreeFeature.Builder> configParameters = Function.identity();
            private Optional<TwoLayersFeatureSize> size = Optional.empty();

            private Builder(TrunkPlacer trunkPlacer, FoliagePlacer foliagePlacer) {
                this.trunkPlacer = trunkPlacer;
                this.foliagePlacer = foliagePlacer;
            }

            public Builder log(Block log) {
                this.logType = log;
                return this;
            }

            public Builder leaves(Block leaves) {
                this.leavesType = leaves;
                return this;
            }

            public Builder configure(Function<TreeFeature.Builder, TreeFeature.Builder> shape) {
                this.configParameters = shape;
                return this;
            }

            public Builder dimensions(int yLevel, int sizeBelowY, int sizeAboveY) {
                this.size = Optional.of(new TwoLayersFeatureSize(yLevel, Math.max(0, sizeBelowY), Math.max(0, sizeAboveY)));
                return this;
            }

            public Feature build() {
                return configParameters.apply(new TreeFeature.Builder(
                        BlockStateProvider.of(logType),
                        trunkPlacer,
                        BlockStateProvider.of(leavesType),
                        foliagePlacer,
                        size.get(), BlockStateProvider.holderOf(Blocks.DIRT)
                    )).build();
            }
        }
    }

    public record Placement(PlacementModifier count, ResourceKey<PlacedFeature> key) { }
}
