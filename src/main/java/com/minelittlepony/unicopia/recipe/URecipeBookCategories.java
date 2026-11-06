package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;

public interface URecipeBookCategories {
    RecipeBookCategory SPELLBOOK = register("spellbook");

    private static RecipeBookCategory register(String id) {
        return Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, Unicopia.id(id), new RecipeBookCategory());
    }


    static void bootstrap() {}
}
