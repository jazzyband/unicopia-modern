package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;

public abstract class ItemCombinationRecipe extends CustomRecipe {
    private final CraftingBookCategory recipeCategory;

    public ItemCombinationRecipe(CraftingBookCategory category) {
        this.recipeCategory = category;
    }

    @Override
    public CraftingBookCategory category() {
        return recipeCategory;
    }

    @Override
    public final boolean matches(CraftingInput inventory, Level world) {
        Tuple<ItemStack, ItemStack> result = runMatch(inventory);

        return !result.getA().isEmpty() && !result.getB().isEmpty();
    }

    protected Tuple<ItemStack, ItemStack> runMatch(CraftingInput inventory) {
        ItemStack bangle = ItemStack.EMPTY;
        ItemStack dust = ItemStack.EMPTY;

        for(int i = 0; i < inventory.size(); i++) {
           ItemStack stack = inventory.getItem(i);

           if (!stack.isEmpty()) {
              if (isContainerItem(stack)) {
                 if (!bangle.isEmpty()) {
                     return new Tuple<>(bangle, dust);
                 }

                 bangle = stack;
              } else {
                 if (!dust.isEmpty() || !isInsertItem(stack)) {
                     return new Tuple<>(bangle, dust);
                 }

                 dust = stack;
              }
           }
        }

        if (!bangle.isEmpty() && isCombinationInvalid(bangle, dust)) {
            return new Tuple<>(ItemStack.EMPTY, ItemStack.EMPTY);
        }

        return new Tuple<>(bangle, dust);
    }

    protected boolean isCombinationInvalid(ItemStack bangle, ItemStack dust) {
        return false;
    }

    protected abstract boolean isContainerItem(ItemStack stack);

    protected abstract boolean isInsertItem(ItemStack stack);
}
