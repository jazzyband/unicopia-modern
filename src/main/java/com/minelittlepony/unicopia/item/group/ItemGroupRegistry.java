package com.minelittlepony.unicopia.item.group;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.FancyBedBlock.SheetPattern;
import com.minelittlepony.unicopia.item.BedsheetsItem;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;

public interface ItemGroupRegistry {
    List<Item> ITEMS = new ArrayList<>();
    Map<ResourceKey<CreativeModeTab>, Set<Item>> REGISTRY = new HashMap<>();

    static List<ItemStack> getVariations(Item item) {
        if (item instanceof MultiItem m) {
            return m.getDefaultStacks();
        }
        return List.of(item.getDefaultInstance());
    }

    static <T extends Item> T register(T item, ResourceKey<CreativeModeTab> group) {
        REGISTRY.computeIfAbsent(group, g -> new LinkedHashSet<>()).add(item);
        return item;
    }

    static Item register(String name, ResourceKey<JukeboxSong> song) {
        return register(name, s -> new Item(s
                .jukeboxPlayable(song)
                .stacksTo(1)
                .rarity(Rarity.RARE)
            ), CreativeModeTabs.TOOLS_AND_UTILITIES);
    }

    static Item register(Race race) {
        return register(race.getId().withPath(p -> p + "_badge"), Item::new);
    }

    static Item register(SheetPattern pattern) {
        return register(pattern.getSerializedName() + "_bed_sheets", s -> new BedsheetsItem(pattern, s.stacksTo(8)), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    }

    static <T extends Item> T register(String name, Function<Item.Properties, T> item, ResourceKey<CreativeModeTab> group) {
        return register(Unicopia.id(name), item, group);
    }

    static <T extends Item> T register(String name, Function<Item.Properties, T> item) {
        return register(Unicopia.id(name), item);
    }

    static <T extends Item> T register(Identifier id, Function<Item.Properties, T> item, ResourceKey<CreativeModeTab> group) {
        return register(register(id, item), group);
    }

    static <T extends Item> T register(Identifier id, Function<Item.Properties, T> itemFunc) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        T item = itemFunc.apply(new Item.Properties().setId(key));
        if (item instanceof BlockItem bi && bi.getBlock() == null) {
            throw new NullPointerException("Registered block item did not have a block " + id);
        }
        ITEMS.add(item);
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    static ResourceKey<CreativeModeTab> createDynamic(String name, ItemLike icon, Supplier<Stream<Item>> items) {
        ResourceKey<CreativeModeTab> key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Unicopia.id(name));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key.identifier(), FabricCreativeModeTab.builder().displayItems((context, entries) -> {
            items.get().forEach(item -> {
                entries.acceptAll(ItemGroupRegistry.getVariations(item));
            });
        }).icon(() -> icon.asItem().getDefaultInstance()).title(Component.translatable(Util.makeDescriptionId("itemGroup", key.identifier()))).build());
        return key;
    }

    static ResourceKey<CreativeModeTab> createGroupFromTag(String name, TagKey<Item> tag, ItemLike icon) {
        return createDynamic(name, icon, () -> {
            return BuiltInRegistries.ITEM.get(tag)
                    .stream()
                    .flatMap(named -> named.stream())
                    .map(entry -> entry.value());
        });
    }

    static void bootstrap() {
        REGISTRY.forEach((group, items) -> {
            CreativeModeTabEvents.modifyOutputEvent(group).register(event -> {
                event.acceptAll(items.stream().map(Item::getDefaultInstance).toList());
            });
        });
    }
}
