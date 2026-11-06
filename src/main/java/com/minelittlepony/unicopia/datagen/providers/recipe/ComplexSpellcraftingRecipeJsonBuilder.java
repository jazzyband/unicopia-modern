package com.minelittlepony.unicopia.datagen.providers.recipe;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public interface ComplexSpellcraftingRecipeJsonBuilder {
    static ComplexSpellcraftingRecipeJsonBuilder create(Factory factory, ItemLike material) {
        return (exporter, recipeId) -> exporter.accept(recipeId, factory.create(IngredientWithSpell.mundane(material)), null);
    }

    public interface Factory {
        Recipe<?> create(IngredientWithSpell material);
    }

    void offerTo(RecipeOutput exporter, final ResourceKey<Recipe<?>> recipeId);
}
