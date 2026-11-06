package com.minelittlepony.unicopia.datagen;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import com.minelittlepony.unicopia.block.ShellsBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.server.world.PineapplePlantFeature;
import com.minelittlepony.unicopia.server.world.gen.ShellsFeature;
import com.minelittlepony.unicopia.server.world.gen.CloudFeature;
import com.minelittlepony.unicopia.server.world.UWorldGen;
import com.minelittlepony.unicopia.server.world.gen.UBiomes;
import com.minelittlepony.unicopia.server.world.gen.UFeatureConfigs;
import com.minelittlepony.unicopia.server.world.gen.UPlacedFeatures;

public class UWorldGenFeatures {

    static void bootstrapConfiguredFeatures(BootstrapContext<Feature> registerable) {
        registerable.register(UFeatureConfigs.PINEAPPLE_PLANT, new PineapplePlantFeature());
        registerable.register(UFeatureConfigs.SHELLS, new ShellsFeature());
        registerable.register(UFeatureConfigs.CLOUD_ISLANDS, new CloudFeature());
        UTreeFeatures.bootstrapConfiguredFeatures(registerable);
    }

    static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> registerable) {
        var configuredFeatures = registerable.lookup(Registries.FEATURE);
        registerable.register(UPlacedFeatures.PINEAPPLE_PLANT, new PlacedFeature(configuredFeatures.getOrThrow(UFeatureConfigs.PINEAPPLE_PLANT), List.of(
            RarityFilter.onAverageOnceEvery(100),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            BiomeFilter.biome()
        )));
        registerable.register(UPlacedFeatures.SHELLS, new PlacedFeature(configuredFeatures.getOrThrow(UFeatureConfigs.SHELLS), List.of(
                RarityFilter.onAverageOnceEvery(1),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_TOP_SOLID,
                BiomeFilter.biome()
        )));
        registerable.register(UPlacedFeatures.CLOUD_ISLANDS, new PlacedFeature(configuredFeatures.getOrThrow(UFeatureConfigs.CLOUD_ISLANDS), List.of(
                RarityFilter.onAverageOnceEvery(20),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(240), VerticalAnchor.belowTop(8)),
                BiomeFilter.biome()
        )));
        UTreeFeatures.bootstrapPlacedFeatures(registerable);
    }

    static void bootstrapBiomes(BootstrapContext<Biome> registerable) {
        registerable.register(UBiomes.SWEET_APPLE_ORCHARD, new Biome.BiomeBuilder()
                    .hasPrecipitation(true)
                    .temperature(0.8F)
                    .downfall(0.8F)
                    .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).build())
                    .setAttribute(net.minecraft.world.attribute.EnvironmentAttributes.WATER_FOG_COLOR, color(329011))
                    .setAttribute(net.minecraft.world.attribute.EnvironmentAttributes.FOG_COLOR, color(12638463))
                    .setAttribute(net.minecraft.world.attribute.EnvironmentAttributes.SKY_COLOR, color(OverworldBiomes.calculateSkyColor(0.8F)))
                    .setAttribute(net.minecraft.world.attribute.EnvironmentAttributes.AMBIENT_SOUNDS, net.minecraft.world.attribute.AmbientSounds.LEGACY_CAVE_SETTINGS)
                    .setAttribute(net.minecraft.world.attribute.EnvironmentAttributes.BACKGROUND_MUSIC, new net.minecraft.world.attribute.BackgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST)))
                    .mobSpawnSettings(applyAll(new MobSpawnSettings.Builder(),
                                BiomeDefaultFeatures::farmAnimals,
                                BiomeDefaultFeatures::commonSpawns
                            ).addSpawn(EntityTypes.WOLF, MobCategory.CREATURE, 5, net.minecraft.util.valueproviders.ConstantInt.of(4))
                            .build())
                    .generationSettings(applyAll(new BiomeGenerationSettings.Builder(registerable.lookup(Registries.PLACED_FEATURE), registerable.lookup(Registries.CARVER)),
                                BiomeDefaultFeatures::addDefaultCarversAndLakes,
                                BiomeDefaultFeatures::addDefaultCrystalFormations,
                                BiomeDefaultFeatures::addDefaultMonsterRoom,
                                BiomeDefaultFeatures::addDefaultUndergroundVariety,
                                BiomeDefaultFeatures::addDefaultSprings,
                                BiomeDefaultFeatures::addSurfaceFreezing,
                                BiomeDefaultFeatures::addDefaultOres,
                                BiomeDefaultFeatures::addDefaultSoftDisks,
                                BiomeDefaultFeatures::addForestFlowers,
                                BiomeDefaultFeatures::addDefaultFlowers,
                                BiomeDefaultFeatures::addForestGrass,
                                BiomeDefaultFeatures::addDefaultMushrooms,
                                builder -> BiomeDefaultFeatures.addDefaultExtraVegetation(builder, false)
                            )
                            .build())
                    .build());
    }

    @SafeVarargs
    static <T> T applyAll(T t, Consumer<T> ...consumers) {
        for (Consumer<T> consumer : consumers) {
            consumer.accept(t);
        }
        return t;
    }

    private static org.joml.Vector3f color(int rgb) {
        return new org.joml.Vector3f((rgb >> 16 & 255) / 255F, (rgb >> 8 & 255) / 255F, (rgb & 255) / 255F);
    }

}
