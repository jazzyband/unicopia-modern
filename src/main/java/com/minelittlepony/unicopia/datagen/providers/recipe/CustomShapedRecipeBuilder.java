package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.util.Untyped;

public abstract class CustomShapedRecipeBuilder<T extends ShapedRecipe, B extends CustomShapedRecipeBuilder<T, B>> implements RecipeBuilder {
    private final RecipeCategory category;
    private final HolderGetter<Item> items;
    private final Item output;
    private final int count;
    private final List<String> pattern = new ArrayList<>();
    private final Map<Character, Ingredient> inputs = new LinkedHashMap<>();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private boolean showNotification = true;

    public CustomShapedRecipeBuilder(HolderGetter<Item> items, RecipeCategory category, ItemLike output, int count) {
        this.items = items;
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public B input(Character c, TagKey<Item> tag) {
        return input(c, Ingredient.of(items.getOrThrow(tag)));
    }

    public B input(Character c, ItemLike itemProvider) {
        return input(c, Ingredient.of(itemProvider));
    }

    public B input(Character c, Ingredient ingredient) {
        Preconditions.checkArgument(!inputs.containsKey(c), "Symbol '" + c + "' is already defined!");
        Preconditions.checkArgument(c != ' ', "Symbol ' ' (whitespace) is reserved and cannot be defined");
        inputs.put(c, ingredient);
        return Untyped.cast(this);
    }

    public B pattern(String patternStr) {
        Preconditions.checkArgument(pattern.isEmpty() || patternStr.length() == pattern.get(0).length(), "Pattern must be the same width on every line!");
        pattern.add(patternStr);
        return Untyped.cast(this);
    }

    @Override
    public B unlockedBy(String key, Criterion<?> criterion) {
        criteria.put(key, criterion);
        return Untyped.cast(this);
    }

    @Override
    public B group(@Nullable String group) {
        this.group = group;
        return Untyped.cast(this);
    }

    public B showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return Untyped.cast(this);
    }

    public Item getResult() {
        return output;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(output));
    }

    @Override
    public void save(RecipeOutput exporter, ResourceKey<Recipe<?>> key) {
        Preconditions.checkState(!criteria.isEmpty(), "No way of obtaining recipe " + key);
        Advancement.Builder builder = exporter.advancement()
            .rewards(AdvancementRewards.Builder.recipe(key))
            .requirements(AdvancementRequirements.Strategy.OR);
        criteria.forEach(builder::addCriterion);
        exporter.accept(key, createRecipe(
                Objects.requireNonNullElse(group, ""),
                RecipeBuilder.determineCraftingBookCategory(category),
                ShapedRecipePattern.of(inputs, pattern),
                new ItemStack(output, count),
                showNotification
            ), builder.build(key.identifier().withPrefix("recipes/" + category.getFolderName() + "/")));
    }

    protected abstract T createRecipe(String group, CraftingBookCategory category, ShapedRecipePattern shape, ItemStack output, boolean showNotification);
}
