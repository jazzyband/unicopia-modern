package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;


public class UChestAdditionsLootTableProvider extends SimpleFabricLootTableSubProvider {
    private final CompletableFuture<HolderLookup.Provider> registryLookup;

    public UChestAdditionsLootTableProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup, LootContextParamSets.CHEST);
        this.registryLookup = registryLookup;
    }

    private HolderSet<Item> tag(TagKey<Item> key) {
        return registryLookup.join().lookupOrThrow(Registries.ITEM).getOrThrow(key);
    }

    @Override
    public String getName() {
        return super.getName() + " Additions";
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> exporter) {
        acceptAdditions((id, builder) -> exporter.accept(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("unicopiamc", id.identifier().getPath())), builder));
    }

    public void acceptAdditions(BiConsumer<ResourceKey<LootTable>, Builder> exporter) {
        exporter.accept(BuiltInLootTables.ABANDONED_MINESHAFT, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(2, 4))
                .add(LootItem.lootTableItem(UItems.GRYPHON_FEATHER).setWeight(2).apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 4))))
        ));
        exporter.accept(BuiltInLootTables.WOODLAND_MANSION, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(2, 4))
                .add(LootItem.lootTableItem(UItems.GRYPHON_FEATHER).setWeight(10).apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 7))))
                .add(LootItem.lootTableItem(UItems.GOLDEN_WING).setWeight(1).apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2))))
                .add(TagEntry.expandTag(tag(UTags.Items.FRESH_APPLES)).setWeight(1).apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 5))))
        ));
        exporter.accept(BuiltInLootTables.VILLAGE_FLETCHER, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(2, 4))
                .add(LootItem.lootTableItem(UItems.GRYPHON_FEATHER).setWeight(10).apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2))))
                .add(LootItem.lootTableItem(UItems.PEGASUS_FEATHER).setWeight(1).apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2))))
        ));
        exporter.accept(BuiltInLootTables.VILLAGE_PLAINS_HOUSE, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(3, 4))
                .add(TagEntry.expandTag(tag(UTags.Items.FRESH_APPLES)).setWeight(1))
                .add(TagEntry.expandTag(tag(UTags.Items.APPLE_SEEDS)).setWeight(1))
        ));

        exporter.accept(BuiltInLootTables.ANCIENT_CITY, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(0, 1))
                .add(LootItem.lootTableItem(UItems.GROGARS_BELL).setWeight(1))
        ));

        exporter.accept(BuiltInLootTables.BURIED_TREASURE, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(1, 4))
                .add(LootItem.lootTableItem(UItems.PEARL_NECKLACE).setWeight(1))
                .add(TagEntry.expandTag(tag(UTags.Items.SHELLS)).setWeight(3))
        ));
        exporter.accept(BuiltInLootTables.SHIPWRECK_SUPPLY, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(1, 6))
                .add(TagEntry.expandTag(tag(UTags.Items.SHELLS)).setWeight(3))
                .add(TagEntry.expandTag(tag(UConventionalTags.Items.ROTTEN_FISH)).setWeight(1))
        ));
        exporter.accept(BuiltInLootTables.SHIPWRECK_TREASURE, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(1, 4))
                .add(LootItem.lootTableItem(UItems.PEARL_NECKLACE).setWeight(1))
                .add(TagEntry.expandTag(tag(UTags.Items.SHELLS)).setWeight(3))
                .add(TagEntry.expandTag(tag(UConventionalTags.Items.ROTTEN_FISH)).setWeight(1))
        ));
        exporter.accept(BuiltInLootTables.UNDERWATER_RUIN_BIG, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(1, 2))
                .add(LootItem.lootTableItem(UItems.PEARL_NECKLACE).setWeight(1))
                .add(LootItem.lootTableItem(UItems.SHELLY).setWeight(4))
                .add(TagEntry.expandTag(tag(UTags.Items.SHELLS)).setWeight(8))
                .add(TagEntry.expandTag(tag(UConventionalTags.Items.ROTTEN_FISH)).setWeight(1))
        ));
        exporter.accept(BuiltInLootTables.UNDERWATER_RUIN_SMALL, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(1, 4))
                .add(TagEntry.expandTag(tag(UTags.Items.SHELLS)).setWeight(1))
                .add(TagEntry.expandTag(tag(UConventionalTags.Items.ROTTEN_FISH)).setWeight(1))
        ));

        exporter.accept(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(1, 4))
                .add(LootItem.lootTableItem(UItems.WEIRD_ROCK).setWeight(2))
                .add(LootItem.lootTableItem(UItems.ROCK).setWeight(1))
                .add(LootItem.lootTableItem(UItems.TOM).setWeight(1))
                .add(LootItem.lootTableItem(UItems.ROCK_STEW).setWeight(1))
                .add(LootItem.lootTableItem(UItems.PEBBLES).setWeight(1))
                .add(LootItem.lootTableItem(UItems.SHELLY).setWeight(1))
                .add(TagEntry.expandTag(tag(UTags.Items.SHELLS)).setWeight(1))
                .add(LootItem.lootTableItem(UItems.PEARL_NECKLACE).setWeight(1))
        ));
        exporter.accept(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(1, 4))
                .add(LootItem.lootTableItem(UItems.MEADOWBROOKS_STAFF).setWeight(2))
                .add(LootItem.lootTableItem(UItems.BOTCHED_GEM).setWeight(3))
                .add(LootItem.lootTableItem(UItems.PEGASUS_FEATHER).setWeight(1))
        ));
        exporter.accept(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(1, 4))
                .add(LootItem.lootTableItem(UItems.BROKEN_SUNGLASSES).setWeight(2))
                .add(LootItem.lootTableItem(UItems.EMPTY_JAR).setWeight(2))
                .add(LootItem.lootTableItem(UItems.MUSIC_DISC_CRUSADE).setWeight(1))
        ));
        exporter.accept(BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(1, 2))
                .add(TagEntry.expandTag(tag(UTags.Items.SHELLS)).setWeight(2))
                .add(LootItem.lootTableItem(UItems.PEARL_NECKLACE).setWeight(1))
                .add(TagEntry.expandTag(tag(UConventionalTags.Items.ROTTEN_FISH)).setWeight(2))
        ));

        exporter.accept(BuiltInLootTables.FISHING, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(1, 2))
                .add(TagEntry.expandTag(tag(UTags.Items.SHELLS)).setWeight(2))
                .add(TagEntry.expandTag(tag(UConventionalTags.Items.ROTTEN_FISH)).setWeight(1))
        ));

        exporter.accept(BuiltInLootTables.FISHING_JUNK, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.between(1, 2))
                .add(LootItem.lootTableItem(UItems.BROKEN_SUNGLASSES).setWeight(2))
                .add(LootItem.lootTableItem(UItems.WHEAT_WORMS).setWeight(2))
                .add(TagEntry.expandTag(tag(UConventionalTags.Items.ROTTEN_FISH)).setWeight(1))
                .add(LootItem.lootTableItem(UItems.BOTCHED_GEM).setWeight(4))
        ));

        exporter.accept(BuiltInLootTables.FISHERMAN_GIFT, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(UItems.PEARL_NECKLACE).setWeight(1))
                .add(LootItem.lootTableItem(UItems.SHELLY).setWeight(1))
        ));
    }

}
