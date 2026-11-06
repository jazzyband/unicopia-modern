package com.minelittlepony.unicopia.mixin;

import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StonecutterMenu.class)
public interface StonecutterScreenHandlerRecipesSetter {
    @Accessor
    void setRecipesForInput(SelectableRecipe.SingleInputSet<? extends StonecutterRecipe> availableRecipes);
}
