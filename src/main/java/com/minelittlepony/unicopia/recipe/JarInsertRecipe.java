package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.item.EmptyJarItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.Appearance;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class JarInsertRecipe extends ItemCombinationRecipe {

    public JarInsertRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public final ItemStack assemble(CraftingInput inventory) {
        return Appearance.set(UItems.FILLED_JAR.getDefaultInstance(), runMatch(inventory).getB());
    }

    @Override
    protected boolean isContainerItem(ItemStack stack) {
        return stack.getItem() == UItems.EMPTY_JAR;
    }

    @Override
    protected boolean isInsertItem(ItemStack stack) {
        return !(stack.getItem() instanceof EmptyJarItem);
    }

    @Override
    protected boolean isCombinationInvalid(ItemStack bangle, ItemStack dust) {
        return false;
    }

    @Override
    public RecipeSerializer<? extends JarInsertRecipe> getSerializer() {
        return URecipes.JAR_INSERT_SERIALIZER;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inventory) {
        return NonNullList.withSize(inventory.size(), ItemStack.EMPTY);
    }
}
