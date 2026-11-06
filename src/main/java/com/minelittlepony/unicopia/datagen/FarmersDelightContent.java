package com.minelittlepony.unicopia.datagen;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Function;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import com.mojang.serialization.Lifecycle;

public interface FarmersDelightContent {
    String DEFAULT_NAMESPACE = "farmersdelight";
    TagKey<Item> CABBAGE_ROLE_INGREDIENTS = TagKey.create(Registries.ITEM, id("cabbage_roll_ingredients"));
    TagKey<Item> COMFORT_FOODS = TagKey.create(Registries.ITEM, id("comfort_foods"));
    TagKey<Item> CABINETS = TagKey.create(Registries.ITEM, id("cabinets"));
    TagKey<Item> CANVAS_SIGNS = TagKey.create(Registries.ITEM, id("canvas_signs"));

    static void bootstrap() { }

    static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(DEFAULT_NAMESPACE, name);
    }
}
