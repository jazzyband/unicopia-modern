package com.minelittlepony.unicopia.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.concurrent.CompletableFuture;


import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public abstract class DataGenRegistryProvider<T> implements SingleRegistryBootstrap<T> {

    private final ResourceKey<Registry<T>> registryRef;
    private final List<ResourceKey<T>> keys = new ArrayList<>();

    public DataGenRegistryProvider(ResourceKey<Registry<T>> registryRef) {
        this.registryRef = registryRef;
    }

    public final void addToPack(Pack pack) {
        pack.addProvider(this::createDataGenerator);
        pack.addProvider(this::createTagProvider);
    }

    protected List<ResourceKey<T>> getKeys() {
        return keys;
    }

    protected abstract void configureTags(TagProvider tagProvider, Provider lookup);

    public FabricDynamicRegistryProvider createDataGenerator(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
        return new FabricDynamicRegistryProvider(output, registriesFuture) {
            @Override
            public String getName() {
                return "Unicopia Registry Data Gen " + registryRef.identifier();
            }

            @Override
            protected void configure(Provider registries, Entries entries) {
                DataGenRegistryProvider.this.run(new BootstrapContext<T>() {
                    @Override
                    public Reference<T> register(ResourceKey<T> key, T value) {
                        keys.add(key);
                        entries.add(key, value);
                        return null;
                    }

                    @Override
                    public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> registryRef) {
                        return registries.lookupOrThrow(registryRef);
                    }

                    @Override
                    public <S> Stream<Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> registryRef) {
                        return registries.lookupOrThrow(registryRef).listElements();
                    }
                });
            }
        };
    }

    public FabricTagsProvider<T> createTagProvider(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
        return new TagProvider(output, registriesFuture);
    }

    protected class TagProvider extends FabricTagsProvider<T> {
        public TagProvider(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
            super(output, DataGenRegistryProvider.this.registryRef, registriesFuture);
        }

        @Override
        public net.minecraft.data.tags.TagAppender<T> tag(TagKey<T> tag) {
            return super.tag(tag);
        }

        @Override
        protected void addTags(Provider lookup) {
            DataGenRegistryProvider.this.configureTags(this, lookup);
        }
    }
}
