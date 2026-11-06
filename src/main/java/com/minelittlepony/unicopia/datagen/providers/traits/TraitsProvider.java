package com.minelittlepony.unicopia.datagen.providers.traits;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.minelittlepony.unicopia.datagen.Datagen;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;

public class TraitsProvider implements DataProvider {
    protected final FabricPackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registryFuture;

    private final DataCollector<TraitLoader.TraitStream> collector;

    public TraitsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        this.output = output;
        this.registryFuture = registryLookup;
        this.collector = new DataCollector<>(output.createPathProvider(Target.DATA_PACK, "traits"), TraitLoader.TraitStream.CODEC);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return registryFuture.thenCompose(registries -> {
            var items = registries.lookupOrThrow(Registries.ITEM);
            generate(registries, collector.prime());
            Debug.testTraitCoverage(items, Datagen.LOGGER, entry -> collector.streamValues()
                    .flatMap(traitStream -> traitStream.entries())
                    .map(Map.Entry::getKey)
                    .anyMatch(key -> key.test(entry)));
            return collector.upload(writer);
        });
    }

    protected void generate(Provider registries, BiConsumer<Identifier, TraitLoader.TraitStream> exporter) {
        new UTraitsGenerator().generate(registries, exporter);
        new VanillaTraitsGenerator().generate(registries, exporter);
        new FarmersDelightTraitsGenerator().generate(registries, exporter);
    }

    @Override
    public String getName() {
        return "Item Trait Groups";
    }

}
