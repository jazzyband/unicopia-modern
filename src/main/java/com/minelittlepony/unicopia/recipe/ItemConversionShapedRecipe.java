package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class ItemConversionShapedRecipe extends ShapedRecipe {
    public static final MapCodec<ItemConversionShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ItemConversionShapedRecipe::group),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ItemConversionShapedRecipe::category),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.raw),
            ItemStackTemplate.CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ItemConversionShapedRecipe::showNotification)
    ).apply(i, ItemConversionShapedRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemConversionShapedRecipe> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ItemConversionShapedRecipe::group,
            PacketCodecUtils.ofEnum(CraftingBookCategory.class), ItemConversionShapedRecipe::category,
            ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.raw,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.base,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
            ByteBufCodecs.BOOL, ItemConversionShapedRecipe::showNotification,
            ItemConversionShapedRecipe::new
    );

    final ShapedRecipePattern raw;
    final ItemStackTemplate base;
    final ItemStackTemplate result;

    public ItemConversionShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern raw, ItemStackTemplate base, ItemStackTemplate result, boolean showNotification) {
        super(new net.minecraft.world.item.crafting.Recipe.CommonInfo(showNotification), new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(category, group), raw, result);
        this.raw = raw;
        this.base = base;
        this.result = result;
    }

    public ItemConversionShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern raw, ItemStack base, ItemStack result, boolean showNotification) {
        this(group, category, raw, ItemStackTemplate.fromNonEmptyStack(base), ItemStackTemplate.fromNonEmptyStack(result), showNotification);
    }

    @Override
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return com.minelittlepony.unicopia.util.Untyped.cast(URecipes.CONVERSION_SERIALIZER);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return input.items().stream()
                .filter(i -> {

                    return i.is(base.item());
                })
                .findFirst()
                .map(base -> base.transmuteCopy(result.item().value()))
                .orElseGet(() -> super.assemble(input));
    }
}
