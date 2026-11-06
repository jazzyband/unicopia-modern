package com.minelittlepony.unicopia.recipe;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.Appearance;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class JarExtractRecipe extends CustomRecipe {
    private final CraftingBookCategory recipeCategory;
    public JarExtractRecipe(CraftingBookCategory category) {
        this.recipeCategory = category;
    }

    @Override
    public CraftingBookCategory category() {
        return recipeCategory;
    }

    @Override
    public final boolean matches(CraftingInput inventory, Level world) {
        return !assemble(inventory).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inventory) {
        ItemStack jar = ItemStack.EMPTY;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (!stack.is(UItems.FILLED_JAR) || !jar.isEmpty() || !Appearance.hasAppearance(stack)) {
                return ItemStack.EMPTY;
            }

            jar = stack;
        }

        return Appearance.upwrapAppearance(jar);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public RecipeSerializer<JarExtractRecipe> getSerializer() {
        return URecipes.JAR_EXTRACT_SERIALIZER;
    }
}
