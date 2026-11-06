package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.Optional;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import com.google.common.base.Predicates;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class SpellShapedCraftingRecipe extends ShapedRecipe {
    public static final MapCodec<SpellShapedCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(SpellShapedCraftingRecipe::group),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(SpellShapedCraftingRecipe::category),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.raw),
            Ingredient.CODEC.optionalFieldOf("spell_source").forGetter(recipe -> recipe.spellSource),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(SpellShapedCraftingRecipe::showNotification)
    ).apply(instance, SpellShapedCraftingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellShapedCraftingRecipe> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SpellShapedCraftingRecipe::group,
            CraftingBookCategory.STREAM_CODEC, SpellShapedCraftingRecipe::category,
            ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.raw,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, recipe -> recipe.spellSource,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
            ByteBufCodecs.BOOL, SpellShapedCraftingRecipe::showNotification,
            SpellShapedCraftingRecipe::new
    );

    private final ShapedRecipePattern raw;
    private final ItemStackTemplate result;

    private final Optional<Ingredient> spellSource;

    public SpellShapedCraftingRecipe(String group, CraftingBookCategory category, ShapedRecipePattern raw, Optional<Ingredient> spellSource, ItemStackTemplate result, boolean showNotification) {
        super(new net.minecraft.world.item.crafting.Recipe.CommonInfo(showNotification),
                new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(category, group), raw,
                result);
        this.spellSource = spellSource;
        this.raw = raw;
        this.result = result;
    }

    public SpellShapedCraftingRecipe(String group, CraftingBookCategory category, ShapedRecipePattern raw, Optional<Ingredient> spellSource, ItemStack result, boolean showNotification) {
        this(group, category, raw, spellSource, ItemStackTemplate.fromStack(result), showNotification);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer)URecipes.CRAFTING_MAGICAL_SERIALIZER;
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        return super.matches(input, world) && (spellSource.isEmpty() || input.items().stream().anyMatch(spellSource.get()));
    }

    public ItemStack assemble(CraftingInput inventory, Provider registries) {
        return assemble(inventory);
    }

    @Override
    public ItemStack assemble(CraftingInput inventory) {
        return inventory.items().stream()
            .filter(spellSource.isPresent() ? spellSource.get() : Predicates.alwaysTrue())
            .filter(EnchantableItem::isEnchanted)
            .map(EnchantableItem::getSpellEffect)
            .findFirst()
            .orElseGet(CustomisedSpellType::empty)
            .applyTo(super.assemble(inventory));
    }
}
