package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AltarRecipe(String group, List<Ingredient> ingredients, ItemStackTemplate resultTemplate) implements Recipe<AltarRecipe.Input> {
    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("group").forGetter(AltarRecipe::group),
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(AltarRecipe::ingredients),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(AltarRecipe::resultTemplate)
    ).apply(instance, AltarRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AltarRecipe::group,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)), AltarRecipe::ingredients,
            ItemStackTemplate.STREAM_CODEC, AltarRecipe::resultTemplate,
            AltarRecipe::new
    );

    public AltarRecipe(String group, List<Ingredient> ingredients, ItemStack result) {
        this(group, ingredients, ItemStackTemplate.fromNonEmptyStack(result));
    }

    public ItemStack result() {
        return resultTemplate.create();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredients);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<AltarRecipe> getSerializer() {
        return URecipes.ALTAR_SERIALIZER;
    }

    @Override
    public RecipeType<AltarRecipe> getType() {
        return URecipes.ALTAR;
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public boolean matches(Input input, Level world) {
        return input.matcher.canCraft(this, null);
    }

    public ItemStack craft(Input input, Provider lookup) {
        var match = toMatch(input, lookup);
        match.consumeInputs(match.getMinCrafts());
        return match.getResult();
    }

    @Override
    public ItemStack assemble(Input input) {
        return craft(input, null);
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    public AltarRecipeMatch toMatch(Input input, Provider lookup) {
        return new AltarRecipeMatch(input.getFilteredInputs(this), result());
    }

    public static class Input implements RecipeInput {
        private final List<ItemEntity> stacks;
        private final StackedItemContents matcher = new StackedItemContents();

        public Input(List<ItemEntity> stacks) {
            this.stacks = stacks;
            stacks.forEach(stack -> matcher.accountStack(stack.getItem()));
        }

        public Stream<ItemEntity> getFilteredInputs(AltarRecipe recipe) {
            return stacks.stream().filter(stack -> {
                return recipe.ingredients().stream().anyMatch(i -> i.test(stack.getItem()));
            });
        }

        @Override
        public ItemStack getItem(int slot) {
            return stacks.get(slot).getItem();
        }

        @Override
        public int size() {
            return stacks.size();
        }
    }
}
