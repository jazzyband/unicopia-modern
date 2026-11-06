package com.minelittlepony.unicopia.server.world.gen;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public interface UPlacedFeatures {
    ResourceKey<PlacedFeature> SHELLS = of("shells");
    ResourceKey<PlacedFeature> PINEAPPLE_PLANT = of("pineapple_plant");
    ResourceKey<PlacedFeature> CLOUD_ISLANDS = of("cloud_islands_placed");

    ResourceKey<PlacedFeature> ZAP_APPLE_TREE = of("zap_apple_tree_placed");
    ResourceKey<PlacedFeature> GREEN_APPLE_TREE = of("green_apple_tree_placed");
    ResourceKey<PlacedFeature> SWEET_APPLE_TREE = of("sweet_apple_tree_placed");
    ResourceKey<PlacedFeature> SWEET_APPLE_TREE_ORCHARD = of("sweet_apple_tree_orchard_placed");
    ResourceKey<PlacedFeature> SOUR_APPLE_TREE = of("sour_apple_tree_placed");
    ResourceKey<PlacedFeature> BANANA_TREE = of("banana_tree_placed");
    ResourceKey<PlacedFeature> MANGO_TREE = of("mango_tree_placed");

    static ResourceKey<PlacedFeature> of(String id) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Unicopia.id(id));
    }
}
