package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.AltarRecipe;

public class AltarRecipeJsonBuilder implements RecipeBuilder {
    private final Map<String, Criterion<?>> criterions = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final RecipeCategory category;

    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final ItemLike result;

    public static AltarRecipeJsonBuilder create(RecipeCategory category, ItemLike result) {
        return new AltarRecipeJsonBuilder(category, result);
    }

    private AltarRecipeJsonBuilder(RecipeCategory category, ItemLike result) {
        this.category = category;
        this.result = result;
    }

    public AltarRecipeJsonBuilder input(ItemLike item) {
        ingredients.add(Ingredient.of(item));
        return this;
    }

    @Override
    public AltarRecipeJsonBuilder unlockedBy(String name, Criterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    @Override
    public AltarRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    public void save(RecipeOutput exporter, ResourceKey<Recipe<?>> key) {
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + key);
        Advancement.Builder advancementBuilder = exporter.advancement()
            .rewards(AdvancementRewards.Builder.recipe(key))
            .requirements(AdvancementRequirements.Strategy.OR);
        criterions.forEach(advancementBuilder::addCriterion);
        exporter.accept(key, new AltarRecipe(
                group == null ? "" : group,
                ingredients,
                new ItemStack(result)
        ), advancementBuilder.build(key.identifier().withPrefix("recipes/" + category.getFolderName() + "/")));
    }

    public Item getResult() {
        return result.asItem();
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(result.asItem()));
    }
}
