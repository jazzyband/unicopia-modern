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
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.Appearance;
import com.minelittlepony.unicopia.recipe.ZapAppleRecipe;

public class TrickCraftingRecipeJsonBuilder implements RecipeBuilder {
    private final Map<String, Criterion<?>> criterions = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final RecipeCategory category;
    private final Item output;

    private final NonNullList<Ingredient> inputs = NonNullList.create();

    public TrickCraftingRecipeJsonBuilder(RecipeCategory category, ItemLike output) {
        this.category = category;
        this.output = output.asItem();
    }

    public static TrickCraftingRecipeJsonBuilder create(RecipeCategory category, ItemLike output) {
        return new TrickCraftingRecipeJsonBuilder(category, output);
    }

    public Item getResult() {
        return output;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(output));
    }

    public TrickCraftingRecipeJsonBuilder input(ItemLike input) {
        inputs.add(Ingredient.of(input));
        return this;
    }

    @Override
    public TrickCraftingRecipeJsonBuilder unlockedBy(String name, Criterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    @Override
    public TrickCraftingRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    public void save(RecipeOutput exporter, ResourceKey<Recipe<?>> key) {
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + key.identifier());
        Advancement.Builder builder = exporter.advancement()
            .rewards(AdvancementRewards.Builder.recipe(key))
            .requirements(AdvancementRequirements.Strategy.OR);
        criterions.forEach(builder::addCriterion);
        exporter.accept(key,
                new ZapAppleRecipe(group == null ? "" : group, CraftingBookCategory.MISC, Appearance.set(UItems.ZAP_APPLE.getDefaultInstance(), output.getDefaultInstance()), inputs),
                builder.build(key.identifier().withPrefix("recipes/" + category.getFolderName() + "/"))
        );
    }
}
