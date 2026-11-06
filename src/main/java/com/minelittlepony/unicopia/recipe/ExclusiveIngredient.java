package com.minelittlepony.unicopia.recipe;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record ExclusiveIngredient(Ingredient include, Ingredient exclude) implements CustomIngredient {
    public static final MapCodec<ExclusiveIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("include").forGetter(ExclusiveIngredient::include),
            Ingredient.CODEC.fieldOf("exclude").forGetter(ExclusiveIngredient::exclude)
    ).apply(i, ExclusiveIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExclusiveIngredient> PACKET_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ExclusiveIngredient::include,
            Ingredient.CONTENTS_STREAM_CODEC, ExclusiveIngredient::exclude,
            ExclusiveIngredient::new
    );

    @Override
    public boolean test(ItemStack stack) {
        return include.test(stack) && !exclude.test(stack);
    }

    @Deprecated
    @Override
    public Stream<Holder<Item>> items() {
        return include.items().filter(i -> test(i.value().getDefaultInstance()));
    }

    @Override
    public boolean requiresTesting() {
        return false;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return URecipes.EXCLUSIVE_INGREDIENT_SERIALIZER;
    }

}
