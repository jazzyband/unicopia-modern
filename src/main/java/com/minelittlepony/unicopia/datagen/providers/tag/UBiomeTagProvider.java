package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.server.world.gen.UBiomes;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

public class UBiomeTagProvider extends FabricTagsProvider<Biome> {
    public UBiomeTagProvider(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
        super(output, Registries.BIOME, registriesFuture);
    }

    @Override
    protected void addTags(Provider lookup) {
        tag(BiomeTags.IS_FOREST)
            .add(UBiomes.SWEET_APPLE_ORCHARD);
    }
}
