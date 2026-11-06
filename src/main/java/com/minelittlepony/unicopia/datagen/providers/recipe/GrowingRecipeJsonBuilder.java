package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.recipe.TransformCropsRecipe;

public class GrowingRecipeJsonBuilder {
    private final Map<String, Criterion<?>> criterions = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final RecipeCategory category;
    private final BlockState output;
    private Block target;
    private BlockState fuel;

    public static GrowingRecipeJsonBuilder create(RecipeCategory category, BlockState output) {
        return new GrowingRecipeJsonBuilder(category, output);
    }

    protected GrowingRecipeJsonBuilder(RecipeCategory category, BlockState output) {
        this.category = category;
        this.output = output;
    }

    public GrowingRecipeJsonBuilder target(Block target) {
        this.target = target;
        return this;
    }

    public GrowingRecipeJsonBuilder fuel(BlockState fuel) {
        this.fuel = fuel;
        return this;
    }

    public GrowingRecipeJsonBuilder criterion(String name, Criterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    public GrowingRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> key) {
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + key.identifier());
        Advancement.Builder advancementBuilder = exporter.advancement()
                .rewards(AdvancementRewards.Builder.recipe(key))
                .requirements(AdvancementRequirements.Strategy.OR);
        criterions.forEach(advancementBuilder::addCriterion);
        exporter.accept(key, new TransformCropsRecipe(target, fuel, output), advancementBuilder.build(key.identifier().withPrefix("recipes/" + category.getFolderName() + "/")));
    }

    public void offerTo(RecipeOutput exporter) {
        offerTo(exporter, ResourceKey.create(Registries.RECIPE, BuiltInRegistries.BLOCK.getKey(output.getBlock())));
    }

    public void offerTo(RecipeOutput exporter, String recipePath) {
        Identifier recipeId = Identifier.parse(recipePath);
        Identifier id = BuiltInRegistries.BLOCK.getKey(output.getBlock());
        if (recipeId.equals(id)) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, ResourceKey.create(Registries.RECIPE, recipeId));
    }
}
