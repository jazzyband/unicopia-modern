package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

public interface UFuels {
    static ResourceKey<ContextIntProvider> ticks(int amount) {
        return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Unicopia.id("cooking/time_" + amount));
    }
}
