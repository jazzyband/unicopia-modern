package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.effect.UPotions;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;


public class UChestLootTableProvider extends SimpleFabricLootTableSubProvider {
    public UChestLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup, LootContextParamSets.CHEST);
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, Builder> exporter) {
        exporter.accept(ResourceKey.create(Registries.LOOT_TABLE, Unicopia.id("chests/changeling_hive_trap")), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(6))

                .add(createTippedArrowEntry(UPotions.MORPH_EARTH_PONY.shortEffect(), 3))
                .add(createTippedArrowEntry(UPotions.MORPH_UNICORN.shortEffect(), 1))
                .add(createTippedArrowEntry(UPotions.MORPH_PEGASUS.shortEffect(), 1))
                .add(createTippedArrowEntry(UPotions.MORPH_BAT.shortEffect(), 1))
                .add(createTippedArrowEntry(UPotions.MORPH_KIRIN.shortEffect(), 1))
                .add(createTippedArrowEntry(UPotions.MORPH_HIPPOGRIFF.shortEffect(), 1))

                .add(createTippedArrowEntry(UPotions.MORPH_EARTH_PONY.longEffect(), 5))
                .add(createTippedArrowEntry(UPotions.MORPH_UNICORN.longEffect(), 2))
                .add(createTippedArrowEntry(UPotions.MORPH_PEGASUS.longEffect(), 2))
                .add(createTippedArrowEntry(UPotions.MORPH_BAT.longEffect(), 2))
                .add(createTippedArrowEntry(UPotions.MORPH_KIRIN.longEffect(), 2))
                .add(createTippedArrowEntry(UPotions.MORPH_HIPPOGRIFF.longEffect(), 2))
        ));
    }

    private static LootItem.Builder<?> createTippedArrowEntry(Holder<Potion> potion, int weight) {
        return LootItem.lootTableItem(Items.TIPPED_ARROW)
                .setWeight(weight)
                .apply(SetPotionFunction.setPotion(potion))
                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(3, 9)));
    }
}
