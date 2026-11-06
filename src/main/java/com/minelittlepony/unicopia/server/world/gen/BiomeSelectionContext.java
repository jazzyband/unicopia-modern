package com.minelittlepony.unicopia.server.world.gen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

public interface BiomeSelectionContext {
    ResourceKey<Biome> biomeKey();

    SplittableBiomeCoordinate referenceFrame();

    @Nullable
    ResourceKey<Biome> addOverride(SplittableBiomeCoordinate coordinate, ResourceKey<Biome> biome);

    public record SplittableBiomeCoordinate(
            SplitableParameterRange temperature,
            SplitableParameterRange humidity,
            SplitableParameterRange continentalness,
            SplitableParameterRange erosion,
            SplitableParameterRange depth,
            SplitableParameterRange weirdness,
            long offset) {
    }

    public interface SplitableParameterRange {
        SplittableBiomeCoordinate splitAbove(float midpoint);

        SplittableBiomeCoordinate splitBelow(float midpoint);
    }
}
