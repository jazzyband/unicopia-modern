package com.minelittlepony.unicopia.datagen.providers.recipe;

import com.minelittlepony.unicopia.recipe.ItemConversionShapedRecipe;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

public class ItemConversionShapedRecipeBuilder extends CustomShapedRecipeBuilder<ItemConversionShapedRecipe, ItemConversionShapedRecipeBuilder> {
    private final Item base;

    private ItemConversionShapedRecipeBuilder(HolderGetter<Item> items, RecipeCategory category, ItemLike base, ItemLike output, int count) {
        super(items, category, output, count);
        this.base = base.asItem();
    }

    public static ItemConversionShapedRecipeBuilder create(HolderGetter<Item> items, RecipeCategory category, ItemLike base, ItemLike output) {
        return create(items, category, base, output, 1);
    }

    public static ItemConversionShapedRecipeBuilder create(HolderGetter<Item> items, RecipeCategory category, ItemLike base, ItemLike output, int count) {
        return new ItemConversionShapedRecipeBuilder(items, category, base, output, count);
    }

    @Override
    protected ItemConversionShapedRecipe createRecipe(String group, CraftingBookCategory category, ShapedRecipePattern shape, ItemStack output, boolean showNotification) {
        return new ItemConversionShapedRecipe(group, category, shape, base.getDefaultInstance(), output, showNotification);
    }
}
