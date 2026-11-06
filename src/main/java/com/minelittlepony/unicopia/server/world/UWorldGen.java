package com.minelittlepony.unicopia.server.world;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.server.world.gen.CaveCarvingStructureProcessor;
import com.minelittlepony.unicopia.server.world.gen.CloudFeature;
import com.minelittlepony.unicopia.server.world.gen.FruitBlobFoliagePlacer;
import com.minelittlepony.unicopia.server.world.gen.StructureExtensions;
import com.minelittlepony.unicopia.server.world.gen.OverworldBiomeSelectionCallback;
import com.minelittlepony.unicopia.server.world.gen.SurfaceGrowthStructureProcessor;
import com.minelittlepony.unicopia.server.world.gen.ShellsFeature;
import com.minelittlepony.unicopia.server.world.gen.UBiomes;

import com.minelittlepony.unicopia.server.world.gen.UFeatures;
import com.minelittlepony.unicopia.server.world.gen.UPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public interface UWorldGen {
    com.mojang.serialization.MapCodec<PineapplePlantFeature> PINEAPPLE_PLANT_FEATURE = Registry.register(BuiltInRegistries.FEATURE_TYPE, Unicopia.id("pineapple_plant"), PineapplePlantFeature.CODEC);
    com.mojang.serialization.MapCodec<ShellsFeature> SHELLS_FEATURE = Registry.register(BuiltInRegistries.FEATURE_TYPE, Unicopia.id("shells"), ShellsFeature.CODEC);

    com.mojang.serialization.MapCodec<SurfaceGrowthStructureProcessor> SURFACE_GROWTH_STRUCTURE_PROCESSOR = Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, Unicopia.id("surface_growth"), SurfaceGrowthStructureProcessor.CODEC);
    com.mojang.serialization.MapCodec<CaveCarvingStructureProcessor> CAVE_CARVING_STRUCTURE_PROCESSOR = Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, Unicopia.id("cave_carving"), CaveCarvingStructureProcessor.CODEC);

    com.mojang.serialization.MapCodec<CloudFeature> CLOUD_FEATURE = Registry.register(BuiltInRegistries.FEATURE_TYPE, Unicopia.id("cloud_islands"), CloudFeature.CODEC);

    FoliagePlacerType<FernFoliagePlacer> FERN_FOLIAGE_PLACER_TYPE = Registry.register(BuiltInRegistries.FOLIAGE_PLACER_TYPE, Unicopia.id("fern_foliage"), new FoliagePlacerType<>(FernFoliagePlacer.CODEC));
    FoliagePlacerType<FruitBlobFoliagePlacer> FRUIT_BLOB_FOLIAGE_PLACER_TYPE = Registry.register(BuiltInRegistries.FOLIAGE_PLACER_TYPE, Unicopia.id("fruit_blob_foliage"), new FoliagePlacerType<>(FruitBlobFoliagePlacer.CODEC));

    static void bootstrap() {
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_JUNGLE), GenerationStep.Decoration.VEGETAL_DECORATION, UPlacedFeatures.PINEAPPLE_PLANT);
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_OCEAN)
                .or(BiomeSelectors.tag(BiomeTags.IS_DEEP_OCEAN)
                .or(BiomeSelectors.tag(BiomeTags.IS_RIVER))
                .or(BiomeSelectors.includeByKey(Biomes.STONY_SHORE))
        ), GenerationStep.Decoration.VEGETAL_DECORATION, UPlacedFeatures.SHELLS);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.TOP_LAYER_MODIFICATION, UPlacedFeatures.CLOUD_ISLANDS);
        UTreeGen.bootstrap();

        OverworldBiomeSelectionCallback.EVENT.register(context -> {
            if (context.biomeKey() == Biomes.FOREST) {
                context.addOverride(context.referenceFrame().temperature().splitAbove(0.9F), UBiomes.SWEET_APPLE_ORCHARD);
            }
        });
        StructureExtensions.bootstrap();
    }
}
