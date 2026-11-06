package com.minelittlepony.unicopia.server.world.gen;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;

public interface UFeatures {
    ResourceKey<Feature> SHELLS = ResourceKey.create(Registries.FEATURE, Unicopia.id("shells"));
    ResourceKey<Feature> PINEAPPLE_PLANT = ResourceKey.create(Registries.FEATURE, Unicopia.id("pineapple_plant"));
}
