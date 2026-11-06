package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.URecipeBookCategories;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.mojang.datafixers.util.Pair;

public interface SpellbookRecipe extends Recipe<SpellbookRecipe.Input> {
    ItemStack craft(Input input, Provider lookup);

    @Override
    default ItemStack assemble(Input input) {
        return craft(input, null);
    }

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default RecipeType<? extends SpellbookRecipe> getType() {
        return URecipes.SPELLBOOK;
    }

    @Override
    default String group() {
        return "unicopia:spellbook";
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return URecipeBookCategories.SPELLBOOK;
    }

    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    int getPriority();

    interface CraftingTreeBuilder {
        void input(ItemStack...stacks);

        void input(Trait...traits);

        void input(Trait trait, float value);

        default void mystery(ItemStack...stacks) {
            input(stacks);
        }

        void result(ItemStack...stack);

        default void input(List<ItemStack> stacks) {
            input(stacks.toArray(ItemStack[]::new));
        }

        default void result(List<ItemStack> stacks) {
            result(stacks.toArray(ItemStack[]::new));
        }

        default void mystery(List<ItemStack> stacks) {
            mystery(stacks.toArray(ItemStack[]::new));
        }
    }

    public record Input(ItemStack stackToModify, List<Pair<Float, ItemStack>> stacks, SpellTraits traits, int gemSlotIndex) implements RecipeInput {
        @Override
        public ItemStack getItem(int slot) {
            return stacks.get(slot).getSecond();
        }

        public float getFactor(int slot) {
            return stacks.get(slot).getFirst();
        }

        @Override
        public int size() {
            return stacks.size();
        }

        public boolean hasIngredients() {
            for (int i = 0; i < size(); i++) {
                if (!getItem(i).isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        public ItemStack getFallbackStack() {
            if (stackToModify().is(UItems.GEMSTONE) || stackToModify().is(UItems.BOTCHED_GEM)) {
                return traits().applyTo(UItems.BOTCHED_GEM.getDefaultInstance());
            }

            return ItemStack.EMPTY;
        }
    }
}
