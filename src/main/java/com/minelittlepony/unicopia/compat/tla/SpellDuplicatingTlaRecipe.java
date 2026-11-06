package com.minelittlepony.unicopia.compat.tla;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;

import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import net.minecraft.world.item.crafting.RecipeHolder;

class SpellDuplicatingTlaRecipe extends SpellbookTlaRecipe {
    public SpellDuplicatingTlaRecipe(RecipeHolder<SpellbookRecipe> recipe) {
        super(recipe);
    }

    @Override
    protected TlaIngredient getOutput() {
        return super.getOutput().withAmount(2);
    }
}
