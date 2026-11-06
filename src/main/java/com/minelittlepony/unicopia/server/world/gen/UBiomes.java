package com.minelittlepony.unicopia.server.world.gen;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public interface UBiomes {
    ResourceKey<Biome> SWEET_APPLE_ORCHARD = of("sweet_apple_orchard");

    static ResourceKey<Biome> of(String name) {
        return ResourceKey.create(Registries.BIOME, Unicopia.id(name));
    }
}
