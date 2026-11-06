package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.util.Untyped;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.Recipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class CloudShapingRecipe extends StonecutterRecipe {
    public static final MapCodec<CloudShapingRecipe> CODEC = SingleItemRecipe.simpleMapCodec(CloudShapingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, CloudShapingRecipe> PACKET_CODEC = SingleItemRecipe.simpleStreamCodec(CloudShapingRecipe::new);

    public CloudShapingRecipe(Recipe.CommonInfo info, Ingredient input, ItemStackTemplate output) {
        super(info, input, output);
    }

    public CloudShapingRecipe(String group, Ingredient input, ItemStack output) {
        this(new Recipe.CommonInfo(true), input, ItemStackTemplate.fromNonEmptyStack(output));
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<StonecutterRecipe> getType() {
        return Untyped.cast(URecipes.CLOUD_SHAPING);
    }

    @Override
    public RecipeSerializer<StonecutterRecipe> getSerializer() {
        return Untyped.cast(URecipes.CLOUD_SHAPING_SERIALIZER);
    }
}
