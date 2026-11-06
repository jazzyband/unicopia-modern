package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;

public class UDimensionTypeTagProvider extends FabricTagsProvider<DimensionType> {
    public UDimensionTypeTagProvider(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
        super(output, Registries.DIMENSION_TYPE, registriesFuture);
    }

    @Override
    protected void addTags(Provider lookup) {
        tag(UTags.DimensionTypes.HAS_NO_ATMOSPHERE)
            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("ad_astra", "earth_orbit")))
            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("ad_astra", "glacio_orbit")))
            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("ad_astra", "mars_orbit")))
            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("ad_astra", "mercury_orbit")))
            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("ad_astra", "moon"))).addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("adastra", "moon_orbit")))
            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("ad_astra", "venus_orbit")))

            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("adastra", "earth_orbit")))
            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("adastra", "glacio_orbit")))
            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("adastra", "mars_orbit")))
            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("adastra", "mercury_orbit")))
            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("adastra", "moon"))).addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("adastra", "moon_orbit")))
            .addOptional(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("adastra", "venus_orbit")));
    }
}
