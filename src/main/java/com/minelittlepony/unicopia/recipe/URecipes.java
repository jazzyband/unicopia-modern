package com.minelittlepony.unicopia.recipe;

import java.util.List;
import java.util.Optional;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.*;
import com.minelittlepony.unicopia.datagen.providers.recipe.CuttingBoardRecipeJsonBuilder;
import com.minelittlepony.unicopia.mixin.RegistryEntryListOwnerAccessor;
import com.minelittlepony.unicopia.server.world.gen.ULootTableEntryType;
import com.minelittlepony.unicopia.util.Untyped;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;

public interface URecipes {
    Codec<List<Ingredient>> SHAPELESS_RECIPE_INGREDIENTS_CODEC = Ingredient.CODEC.listOf(1, 9);

    RecipeType<SpellbookRecipe> SPELLBOOK = register("spellbook");
    RecipeType<CloudShapingRecipe> CLOUD_SHAPING = register("cloud_shaping");
    RecipeType<TransformCropsRecipe> GROWING = register("growing");
    RecipeType<AltarRecipe> ALTAR = register("altar");

    RecipeSerializer<ZapAppleRecipe> ZAP_APPLE_SERIALIZER = register("crafting_zap_apple", ZapAppleRecipe.CODEC, ZapAppleRecipe.PACKET_CODEC);
    RecipeSerializer<ItemConversionShapedRecipe> CONVERSION_SERIALIZER = register("conversion", ItemConversionShapedRecipe.CODEC, ItemConversionShapedRecipe.PACKET_CODEC);
    RecipeSerializer<GlowingRecipe> GLOWING_SERIALIZER = register("crafting_glowing", MapCodec.unit(new GlowingRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC)), StreamCodec.unit(new GlowingRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC)));
    RecipeSerializer<JarInsertRecipe> JAR_INSERT_SERIALIZER = register("jar_insert", MapCodec.unit(new JarInsertRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC)), StreamCodec.unit(new JarInsertRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC)));
    RecipeSerializer<JarExtractRecipe> JAR_EXTRACT_SERIALIZER = register("jar_extract", MapCodec.unit(new JarExtractRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC)), StreamCodec.unit(new JarExtractRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC)));
    RecipeSerializer<SpellShapedCraftingRecipe> CRAFTING_MAGICAL_SERIALIZER = register("crafting_magical", SpellShapedCraftingRecipe.CODEC, SpellShapedCraftingRecipe.PACKET_CODEC);
    RecipeSerializer<SpellCraftingRecipe> TRAIT_REQUIREMENT = register("spellbook/crafting", SpellCraftingRecipe.CODEC, SpellCraftingRecipe.PACKET_CODEC);
    RecipeSerializer<SpellEnhancingRecipe> TRAIT_COMBINING = register("spellbook/combining", SpellEnhancingRecipe.CODEC, SpellEnhancingRecipe.PACKET_CODEC);
    RecipeSerializer<SpellDuplicatingRecipe> SPELL_DUPLICATING = register("spellbook/duplicating", SpellDuplicatingRecipe.CODEC, SpellDuplicatingRecipe.PACKET_CODEC);
    RecipeSerializer<CloudShapingRecipe> CLOUD_SHAPING_SERIALIZER = register("cloud_shaping", CloudShapingRecipe.CODEC, CloudShapingRecipe.PACKET_CODEC);
    RecipeSerializer<TransformCropsRecipe> TRANSFORM_CROP_SERIALIZER = register("transform_crop", TransformCropsRecipe.CODEC, TransformCropsRecipe.STREAM_CODEC);
    RecipeSerializer<AltarRecipe> ALTAR_SERIALIZER = register("altar", AltarRecipe.CODEC, AltarRecipe.STREAM_CODEC);

    CustomIngredientSerializer<ExclusiveIngredient> EXCLUSIVE_INGREDIENT_SERIALIZER = registerIngredient("exclusive", ExclusiveIngredient.CODEC, ExclusiveIngredient.PACKET_CODEC);

    static <T extends Recipe<?>> RecipeType<T> register(String name) {
        Identifier id = Unicopia.id(name);
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<T>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    static <T extends Recipe<?>> RecipeSerializer<T> register(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> packetCodec) {
        return register(name, new RecipeSerializer<>(codec, packetCodec));
    }

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String name, S serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Unicopia.id(name), serializer);
    }

    static <T extends CustomIngredient> CustomIngredientSerializer<T> registerIngredient(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> packetCodec) {
        var serializer = new CustomIngredientSerializer<T>() {
            private final Identifier id = Unicopia.id(name);

            @Override
            public Identifier getIdentifier() {
                return id;
            }

            @Override
            public MapCodec<T> getCodec() {
                return codec;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, T> getStreamCodec() {
                return packetCodec;
            }
        };
        CustomIngredientSerializer.register(serializer);
        return serializer;
    }

    @SuppressWarnings("deprecation")
    static Optional<HolderSet<Item>> getRecipeContentsReplacement(Holder<Item> stack) {
        if (stack.is(Items.STICK.builtInRegistryHolder())) {
            BuiltInRegistries.ITEM.get(ConventionalItemTags.WOODEN_RODS);
            return lookupTag(ConventionalItemTags.WOODEN_RODS, stack);
        }
        if (stack.is(Items.FEATHER.builtInRegistryHolder())) {
            return lookupTag(UTags.Items.MAGIC_FEATHERS, stack);
        }
        return Optional.empty();
    }

    @SuppressWarnings("deprecation")
    private static Optional<HolderSet<Item>> lookupTag(TagKey<Item> tag, Holder<Item> canonicalParent) {
        Registry<Item> registry = canonicalParent instanceof RegistryEntryListOwnerAccessor accessor && accessor.getOwner() instanceof Registry r ? Untyped.cast(r) :BuiltInRegistries.ITEM;

        return registry.get(tag)
                .<HolderSet<Item>>map(Untyped::cast)
                .or(() -> Optional.of(HolderSet.emptyNamed(registry, tag)));
    }

    static void bootstrap() {
        URecipeBookCategories.bootstrap();
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && !FabricLoader.getInstance().isModLoaded("farmersdelight")) {
            CuttingBoardRecipeJsonBuilder.CuttingBoardRecipe.bootstrap();
        }
        URecipeDisplays.bootstrap();
        ULootTableEntryType.bootstrap();
    }
}
