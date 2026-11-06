package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;


public class UEntityLootTableProvider extends SimpleFabricLootTableSubProvider {
    private final CompletableFuture<HolderLookup.Provider> registryLookup;

    public UEntityLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup, LootContextParamSets.ENTITY);
        this.registryLookup = registryLookup;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, Builder> exporter) {
        generate(registryLookup.join(), (type, builder) -> exporter.accept(ResourceKey.create(Registries.LOOT_TABLE, EntityType.getKey(type).withPrefix("entities/")), builder));
    }

    protected void generate(HolderLookup.Provider registryLookup, BiConsumer<EntityType<?>, Builder> exporter) {
        exporter.accept(UEntities.BUTTERFLY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(UItems.BUTTERFLY)
                    .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registryLookup.lookupOrThrow(Registries.ENCHANTMENT), ContextFloatProviders.between(0, 1))))));
        exporter.accept(UEntities.STORM_CLOUD, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(UItems.CLOUD_LUMP)
                    .apply(SetItemCountFunction.setCount(ContextIntProviders.between(6, 12)))
                    .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registryLookup.lookupOrThrow(Registries.ENCHANTMENT), ContextFloatProviders.between(0, 1))))));
        exporter.accept(UEntities.LOOT_BUG, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(TagEntry.tagContents(registryLookup.lookupOrThrow(Registries.ITEM).getOrThrow(UTags.Items.LOOT_BUG_COMMON_DROPS))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(6, 12)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registryLookup.lookupOrThrow(Registries.ENCHANTMENT), ContextFloatProviders.between(0, 3))))
                .add(TagEntry.tagContents(registryLookup.lookupOrThrow(Registries.ITEM).getOrThrow(UTags.Items.LOOT_BUG_RARE_DROPS))
                        .when(LootItemRandomChanceCondition.randomChance(0.25F))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registryLookup.lookupOrThrow(Registries.ENCHANTMENT), ContextFloatProviders.between(0, 6))))
                .add(TagEntry.tagContents(registryLookup.lookupOrThrow(Registries.ITEM).getOrThrow(UTags.Items.LOOT_BUG_EPIC_DROPS))
                        .when(LootItemRandomChanceCondition.randomChance(0.1F))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registryLookup.lookupOrThrow(Registries.ENCHANTMENT), ContextFloatProviders.between(0, 2))))
        ));
    }
}
