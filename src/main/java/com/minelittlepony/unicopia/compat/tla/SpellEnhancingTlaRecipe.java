package com.minelittlepony.unicopia.compat.tla;

import java.util.Arrays;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellEnhancingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;

import io.github.mattidragon.tlaapi.api.recipe.TlaStack;

class SpellEnhancingTlaRecipe extends SpellbookTlaRecipe {

    private final Identifier id;

    public SpellEnhancingTlaRecipe(RecipeHolder<SpellbookRecipe> recipe, Trait trait) {
        super(recipe);
        id = recipe.id().identifier().withPath(p -> p + "/" + trait.getId().getPath());
        input(trait);
        getOutputs().addAll(
                Arrays.stream(((SpellEnhancingRecipe)recipe.value()).getBaseMaterial().getMatchingStacks())
                .map(stack -> TlaStack.of(SpellTraits.of(stack).add(SpellTraits.of(trait, 1)).applyTo(stack)))
                .toList()
        );
    }

    @Override
    public Identifier getId() {
        return id;
    }
}
