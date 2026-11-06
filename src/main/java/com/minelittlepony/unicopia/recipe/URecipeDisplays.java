package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipeDisplay;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public interface URecipeDisplays {

    static void bootstrap() {
        register("spellbook", SpellbookRecipeDisplay.SERIALIZER);
    }

    static <T extends RecipeDisplay> void register(String name, RecipeDisplay.Type<T> serializer) {
        Registry.register(BuiltInRegistries.RECIPE_DISPLAY, Unicopia.id(name), serializer);
    }
}
