package com.minelittlepony.unicopia.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.Appearance;

public class ZapAppleRecipe extends ShapelessRecipe {
    public static final MapCodec<ZapAppleRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ZapAppleRecipe::group),
            CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(ZapAppleRecipe::category),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("appearance").forGetter(recipe -> recipe.appearance),
            URecipes.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(r -> r.ingredients)
        ).apply(instance, ZapAppleRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ZapAppleRecipe> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ZapAppleRecipe::group,
            CraftingBookCategory.STREAM_CODEC, ZapAppleRecipe::category,
            ByteBufCodecs.registry(Registries.ITEM), recipe -> recipe.appearance,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.ingredients,
            ZapAppleRecipe::new
    );

    final Item appearance;
    final List<Ingredient> ingredients;

    public ZapAppleRecipe(String group, CraftingBookCategory category, Item appearance, List<Ingredient> input) {
        super(new net.minecraft.world.item.crafting.Recipe.CommonInfo(true), new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(category, group), new ItemStackTemplate(UItems.ZAP_APPLE), input);
        this.appearance = appearance;
        this.ingredients = input;
    }

    public ZapAppleRecipe(String group, CraftingBookCategory category, ItemStack output, List<Ingredient> input) {
        this(group, category, Appearance.upwrapAppearance(output).getItem(), input);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return Appearance.set(super.assemble(input), appearance.getDefaultInstance());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer)URecipes.ZAP_APPLE_SERIALIZER;
    }
}
