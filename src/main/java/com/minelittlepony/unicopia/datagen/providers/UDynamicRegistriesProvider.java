package com.minelittlepony.unicopia.datagen.providers;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.RegistryDataLoader;

public class UDynamicRegistriesProvider extends FabricDynamicRegistryProvider {
    private final Set<String> namespaces = Set.of(Unicopia.DEFAULT_NAMESPACE, Unicopia.VANILLA_EXTENSIONS_NAMESPACE);

    public UDynamicRegistriesProvider(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Unicopia Registries";
    }

    @Override
    protected void configure(Provider registries, Entries entries) {
        RegistryDataLoader.WORLD_REGISTRIES.forEach(registry -> {
            addAll(entries, entries.getLookups().lookupOrThrow(registry.key()));
        });
    }

    private <T> void addAll(Entries entries, HolderLookup.RegistryLookup<T> wrapper) {
        wrapper.listElementIds()
            .filter(registryKey -> namespaces.contains(registryKey.identifier().getNamespace()))
            .forEach(key -> entries.add(wrapper, key));
    }
}
