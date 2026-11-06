package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.List;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.inventory.IngredientSlot;

public class SpellbookRecipeBookWidget extends RecipeBookComponent<SpellbookScreenHandler> {
    private static final Component TOGGLE_CRAFTABLE_TEXT = Component.translatable("gui.recipebook.toggleRecipes.craftable");
    private static final WidgetSprites TEXTURES = new WidgetSprites(
        Identifier.withDefaultNamespace("recipe_book/filter_enabled"),
        Identifier.withDefaultNamespace("recipe_book/filter_disabled"),
        Identifier.withDefaultNamespace("recipe_book/filter_enabled_highlighted"),
        Identifier.withDefaultNamespace("recipe_book/filter_disabled_highlighted")
    );

    public SpellbookRecipeBookWidget(SpellbookScreenHandler craftingScreenHandler) {
        super(craftingScreenHandler, List.of());
    }

    @Override
    protected WidgetSprites getFilterButtonTextures() {
        return TEXTURES;
    }

    @Override
    protected boolean isCraftingSlot(Slot slot) {
        return slot instanceof IngredientSlot;
    }

    @Override
    protected void selectMatchingRecipes(RecipeCollection recipeResultCollection, StackedItemContents recipeFinder) {
        recipeResultCollection.selectRecipes(recipeFinder, recipe -> {
            return false;
        });
    }

    @Override
    protected Component getRecipeFilterName() {
        return TOGGLE_CRAFTABLE_TEXT;
    }

    @Override
    protected void fillGhostRecipe(GhostSlots ghostRecipe, RecipeDisplay display, ContextMap context) {
        // TODO Auto-generated method stub

    }
}
