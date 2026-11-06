package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellShapedCraftingRecipe;

public class SpellShapedRecipeJsonBuilder extends CustomShapedRecipeBuilder<SpellShapedCraftingRecipe, SpellShapedRecipeJsonBuilder> {
    @Nullable
    private Ingredient spellCopySource;

    private SpellShapedRecipeJsonBuilder(HolderGetter<Item> items, RecipeCategory category, ItemLike output, int count) {
        super(items, category, output, count);
    }

    public static SpellShapedRecipeJsonBuilder create(HolderGetter<Item> items, RecipeCategory category, ItemLike output) {
        return create(items, category, output, 1);
    }

    public static SpellShapedRecipeJsonBuilder create(HolderGetter<Item> items, RecipeCategory category, ItemLike output, int count) {
        return new SpellShapedRecipeJsonBuilder(items, category, output, count);
    }

    public SpellShapedRecipeJsonBuilder withSpellFrom(ItemLike item) {
        return withSpellFrom(Ingredient.of(item));
    }

    public SpellShapedRecipeJsonBuilder withSpellFrom(Ingredient ingredient) {
        spellCopySource = ingredient;
        return this;
    }

    @Override
    protected SpellShapedCraftingRecipe createRecipe(String group, CraftingBookCategory category, ShapedRecipePattern shape, ItemStack output, boolean showNotification) {
        return new SpellShapedCraftingRecipe(group, category, shape, Optional.ofNullable(spellCopySource), output, showNotification);
    }
}
