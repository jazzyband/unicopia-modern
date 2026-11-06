package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

public class UStatusEffectTagProvider extends FabricTagsProvider<MobEffect> {
    public UStatusEffectTagProvider(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
        super(output, Registries.MOB_EFFECT, registriesFuture);
    }

    @Override
    protected void addTags(Provider lookup) {
        tag(UTags.StatusEffects.PINEAPPLE_EFFECTS).add(
                MobEffects.REGENERATION.unwrapKey().orElseThrow(), MobEffects.ABSORPTION.unwrapKey().orElseThrow(), MobEffects.LUCK.unwrapKey().orElseThrow(), MobEffects.HASTE.unwrapKey().orElseThrow()
        );
    }
}
