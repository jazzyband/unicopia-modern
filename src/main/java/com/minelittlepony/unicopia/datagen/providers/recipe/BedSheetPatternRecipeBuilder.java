package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Arrays;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.ExclusiveIngredient;

public class BedSheetPatternRecipeBuilder {
    record PatternTemplate(int[] symbols, int[] uniqueSymbols, String[] pattern) {
        static final PatternTemplate ONE_COLOR = new PatternTemplate("###", "# #", " ##");
        static final PatternTemplate TWO_COLOR = new PatternTemplate("#%#", "% %", " %#");
        static final PatternTemplate THREE_COLOR = new PatternTemplate("cvc", "h h", " vc");
        static final PatternTemplate FOUR_COLOR = new PatternTemplate("wgb", "p g", " pw");
        static final PatternTemplate SEVEN_COLOR = new PatternTemplate("roy", "l b", " pg");

        PatternTemplate(String...pattern) {
            this(Arrays.stream(pattern).flatMapToInt(l -> l.chars()).filter(ch -> ch != ' ').toArray(), pattern);
        }

        PatternTemplate(int[] symbols, String[] pattern) {
            this(symbols, Arrays.stream(symbols).distinct().toArray(), pattern);
        }

        void offerWithoutConversion(RecipeProvider generator, HolderGetter<Item> items,RecipeOutput exporter, ItemLike output, ItemLike...wool) {
            offerRecipe(generator, items, this, exporter, output, wool);
        }

        void offerTo(RecipeProvider generator, HolderGetter<Item> items, RecipeOutput exporter, ItemLike output, ItemLike...wool) {
            offerRecipe(generator, items, this, exporter, output, wool);
            offerBedSheetConversionRecipe(generator, items, this, exporter, output, wool);
        }
    }

    private static void offerRecipe(RecipeProvider generator, HolderGetter<Item> items, PatternTemplate template,
            RecipeOutput exporter,
            ItemLike output,
            ItemLike...wool) {
        var builder = ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS, output);
        for (int i = 0; i < template.uniqueSymbols().length; i++) {
            builder.define((char)template.uniqueSymbols()[i], wool[i]);
        }
        for (int i = 0; i < template.pattern().length; i++) {
            builder.pattern(template.pattern()[i]);
        }
        Arrays.asList(wool).stream().distinct().forEach(input -> {
            builder.unlockedBy(RecipeProvider.getHasName(input), generator.has(input));
        });
        builder.group("bed_sheet").save(exporter);
    }

    private static void offerBedSheetConversionRecipe(RecipeProvider generator, HolderGetter<Item> items, PatternTemplate template, RecipeOutput exporter, ItemLike output, ItemLike...wool) {
        var builder = ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS, output, 2);

        for (int i = 0; i < template.uniqueSymbols().length; i++) {
            builder.define((char)template.uniqueSymbols()[i], wool[i]);
        }
        builder.define('X', new ExclusiveIngredient(Ingredient.of(items.getOrThrow(UTags.Items.WOOL_BED_SHEETS)), Ingredient.of(output)).toVanilla());
        for (int i = 0; i < template.pattern().length - 1; i++) {
            builder.pattern(template.pattern()[i]);
        }
        builder.pattern(template.pattern()[template.pattern().length - 1].replace(' ', 'X'));
        builder
            .unlockedBy("has_bed_sheet", generator.has(output))
            .group("bed_sheet_convert")
            .save(exporter, ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(output.asItem()).withPath(RecipeProvider.getConversionRecipeName(output, UItems.WHITE_BED_SHEETS))));
    }

}
