package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public interface UCompostables {
    ResourceKey<ContextIntProvider> VERY_LOW = ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Unicopia.id("compostable/very_low"));
    ResourceKey<ContextIntProvider> LOW = ContextIntProviders.COMPOSTABLE_LOW;
    ResourceKey<ContextIntProvider> LOW_MEDIUM = ContextIntProviders.COMPOSTABLE_LOW_MEDIUM;
    ResourceKey<ContextIntProvider> MEDIUM = ContextIntProviders.COMPOSTABLE_MEDIUM;
    ResourceKey<ContextIntProvider> HIGH = ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Unicopia.id("compostable/high"));
    ResourceKey<ContextIntProvider> ALWAYS = ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE;
}
