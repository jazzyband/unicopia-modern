package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;

public class UEntityAdditionsLootTableProvider extends SimpleFabricLootTableSubProvider {
    private final CompletableFuture<HolderLookup.Provider> registryLookup;

    public UEntityAdditionsLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup, LootContextParamSets.ENTITY);
        this.registryLookup = registryLookup;
    }

    @Override
    public String getName() {
        return super.getName() + " Additions";
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, Builder> exporter) {
        generate(registryLookup.join(), (type, builder) -> exporter.accept(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("unicopiamc", EntityType.getKey(type).withPrefix("entities/").getPath())), builder));
    }

    protected void generate(HolderLookup.Provider registryLookup, BiConsumer<EntityType<?>, Builder> exporter) {
        exporter.accept(EntityTypes.FROG, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(UItems.FROG_LEGS)
                    .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registryLookup.lookupOrThrow(Registries.ENCHANTMENT), ContextFloatProviders.exactly(2))))));
    }
}
