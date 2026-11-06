package com.minelittlepony.unicopia.server.world;

import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.block.UBlocks;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.biome.v1.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.server.dedicated.Settings;

public record Tree (
        ResourceKey<Feature> feature,
        Optional<Block> sapling,
        Optional<Block> pot
    ) {
    public static final List<Tree> REGISTRY = new ArrayList<>();

    public static class Builder {
        public static final Predicate<BiomeSelectionContext> IS_FOREST = BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(BiomeTags.IS_FOREST));
        public static final Predicate<BiomeSelectionContext> IS_OAK_FOREST = IS_FOREST
                .and(BiomeSelectors.excludeByKey(Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST))
                .and(BiomeSelectors.tag(BiomeTags.IS_TAIGA).negate());

        public static Builder create(ResourceKey<Feature> featureKey) {
            return new Builder(featureKey);
        }

        private Optional<Identifier> saplingId = Optional.empty();
        private BiFunction<TreeGrower, BlockBehaviour.Properties, SaplingBlock> saplingConstructor = SaplingBlock::new;

        private final ResourceKey<Feature> featureKey;

        private final List<Pair<ResourceKey<PlacedFeature>, Predicate<BiomeSelectionContext>>> placements = new ArrayList<>();

        private Builder(ResourceKey<Feature> featureKey) {
            this.featureKey = featureKey;
        }

        public Builder spawns(ResourceKey<PlacedFeature> placement, Predicate<BiomeSelectionContext> selector) {
            placements.add(Pair.of(placement, selector));
            return this;
        }

        public Builder sapling(Identifier saplingId) {
            this.saplingId = Optional.of(saplingId);
            return this;
        }

        public Builder sapling(BiFunction<TreeGrower, BlockBehaviour.Properties, SaplingBlock> constructor) {
            saplingConstructor = constructor;
            return this;
        }

        public Tree build() {
            Optional<Block> sapling = saplingId.map(id -> UBlocks.register(id, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING), s -> saplingConstructor.apply(
                    new TreeGrower(id.toString(), WeightedList.of(),
                            WeightedList.of(featureKey),
                            WeightedList.of(), featureKey
                    ), s), CreativeModeTabs.NATURAL_BLOCKS));
            var pot = sapling.map(saplingBlock -> {
                Block flowerPot = UBlocks.register(
                        saplingId.get().withPrefix("potted_"),
                        Blocks.flowerPotProperties(),
                        s -> new FlowerPotBlock(saplingBlock, s),
                        null
                );
                UBlocks.TRANSLUCENT_BLOCKS.add(flowerPot);
                return flowerPot;
            });
            placements.forEach(placement -> {
                BiomeModifications.addFeature(placement.getSecond(), GenerationStep.Decoration.VEGETAL_DECORATION, placement.getFirst());
            });
            Tree tree = new Tree(featureKey, sapling, pot);
            REGISTRY.add(tree);
            return tree;
        }
    }
}
