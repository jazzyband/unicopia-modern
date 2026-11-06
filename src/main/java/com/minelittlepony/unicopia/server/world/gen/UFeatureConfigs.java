package com.minelittlepony.unicopia.server.world.gen;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;

public interface UFeatureConfigs {
    ResourceKey<Feature> PINEAPPLE_PLANT = of("pineapple_plant");
    ResourceKey<Feature> SHELLS = of("shells");
    ResourceKey<Feature> CLOUD_ISLANDS = of("cloud_islands");

    ResourceKey<Feature> ZAP_APPLE_TREE = of("zap_apple_tree");
    ResourceKey<Feature> GREEN_APPLE_TREE = of("green_apple_tree");
    ResourceKey<Feature> SWEET_APPLE_TREE = of("sweet_apple_tree");
    ResourceKey<Feature> SOUR_APPLE_TREE = of("sour_apple_tree");
    ResourceKey<Feature> GOLDEN_OAK_TREE = of("golden_oak_tree");
    ResourceKey<Feature> BANANA_TREE = of("banana_tree");
    ResourceKey<Feature> MANGO_TREE = of("mango_tree");

    static ResourceKey<Feature> of(String name) {
        return ResourceKey.create(Registries.FEATURE, Unicopia.id(name));
    }
}
