package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Map;
import java.util.NoSuchElementException;

import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.recipe.CloudShapingRecipe;
import com.mojang.datafixers.util.Either;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public interface CraftingMaterialHelper {
    Map<String, TagKey<Item>> MATERIALS = Map.of(
            "wood", ItemTags.PLANKS,
            "stone", ItemTags.STONE_TOOL_MATERIALS,
            "iron", ConventionalItemTags.IRON_INGOTS,
            "gold", ConventionalItemTags.GOLD_INGOTS,
            "copper", ConventionalItemTags.COPPER_INGOTS,
            "netherite", ConventionalItemTags.NETHERITE_INGOTS
    );

    default RecipeProvider asGenerator() {
        return (RecipeProvider)this;
    }

    default Either<ItemLike, TagKey<Item>> getMaterial(Item output, String toStrip, String suffex) {
        Identifier id = BuiltInRegistries.ITEM.getKey(output).withPath(p -> p.replace(toStrip, "") + suffex);
        TagKey<Item> tag = MATERIALS.getOrDefault(id.getPath().replace("en_", "_").split("_")[0], null);
        if (tag != null) {
            return Either.right(tag);
        }
        return Either.left(
            BuiltInRegistries.ITEM.getOptional(id)
                .or(() -> BuiltInRegistries.ITEM.getOptional(Identifier.withDefaultNamespace(id.getPath())))
                .or(() -> BuiltInRegistries.ITEM.getOptional(Identifier.withDefaultNamespace(id.getPath().replace(suffex, ""))))
                .orElseThrow(() -> new NoSuchElementException("No item with id " + id))
        );
    }

    default Item getItem(Identifier id) {
        return BuiltInRegistries.ITEM.getOptional(id).orElseThrow(() -> new NoSuchElementException("No item with id " + id));
    }

    default ShapedRecipeBuilder input(ShapedRecipeBuilder builder, char key, Either<ItemLike, TagKey<Item>> material) {
        material.ifLeft(i -> builder.define(key, i));
        material.ifRight(i -> builder.define(key, i));
        return builder;
    }

    default Criterion<?> conditionsFromEither(Either<ItemLike, TagKey<Item>> material) {
        return material.map(asGenerator()::has, asGenerator()::has);
    }

    default String hasEither(Either<ItemLike, TagKey<Item>> material) {
        return material.map(RecipeProvider::getHasName, this::hasTag);
    }

    default String hasTag(TagKey<Item> tag) {
        return "has_" + tag.location();
    }

    default Criterion<?> conditionsFromSpell(HolderGetter<Item> items, ItemLike gem, SpellType<?> spell) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item()
                .of(items, gem)
                .withComponents(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.expect(UDataComponentTypes.STORED_SPELL, spell)).build())
                .build()
        );
    }

    default String hasSpell(SpellType<?> spell) {
        return "has_" + spell.getId() + "_gemstone";
    }

    default SingleItemRecipeBuilder createCloudShaping(Ingredient input, RecipeCategory category, ItemLike output, int count) {
        return new SingleItemRecipeBuilder(category, CloudShapingRecipe::new, input, output, count);
    }
}
