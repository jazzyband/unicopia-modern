package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.item.GlowableItem;
import net.minecraft.core.HolderLookup.Provider;
import com.minelittlepony.unicopia.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class GlowingRecipe extends ItemCombinationRecipe {

    public GlowingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public final ItemStack assemble(CraftingInput inventory) {
        Tuple<ItemStack, ItemStack> pair = runMatch(inventory);

        ItemStack result = pair.getA().copy();

        GlowableItem.setGlowing(result, pair.getB().getItem() == Items.GLOWSTONE_DUST);

        return result;
    }

    @Override
    protected boolean isContainerItem(ItemStack stack) {
        return stack.getItem() instanceof GlowableItem;
    }

    @Override
    protected boolean isInsertItem(ItemStack stack) {
        return stack.getItem() == Items.GLOWSTONE_DUST || stack.getItem() == Items.INK_SAC;
    }

    @Override
    protected boolean isCombinationInvalid(ItemStack bangle, ItemStack dust) {
        return (dust.getItem() == Items.GLOWSTONE_DUST) == GlowableItem.isGlowing(bangle);
    }

    @Override
    public RecipeSerializer<? extends GlowingRecipe> getSerializer() {
        return URecipes.GLOWING_SERIALIZER;
    }
}
