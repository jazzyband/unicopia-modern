package com.minelittlepony.unicopia.datagen.providers.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface SereneSeasonsTags {
    interface Items {
        TagKey<Item> AUTUMN_CROPS = item("autumn_crops");
        TagKey<Item> WINTER_CROPS = item("winter_crops");
        TagKey<Item> SPRING_CROPS = item("spring_crops");
        TagKey<Item> SUMMER_CROPS = item("summer_crops");

        private static TagKey<Item> item(String name) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("sereneseasons", name));
        }
    }

    interface Blocks {
        TagKey<Block> AUTUMN_CROPS = block("autumn_crops");
        TagKey<Block> WINTER_CROPS = block("winter_crops");
        TagKey<Block> SPRING_CROPS = block("spring_crops");
        TagKey<Block> SUMMER_CROPS = block("summer_crops");

        private static TagKey<Block> block(String name) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("sereneseasons", name));
        }
    }

}
