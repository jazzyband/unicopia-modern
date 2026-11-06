package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily.Variant;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.FarmersDelightContent;
import com.minelittlepony.unicopia.datagen.UBlockFamilies;
import com.minelittlepony.unicopia.item.UItems;

public class UFarmersDelightRecipeGenerator extends RecipeProvider implements CraftingMaterialHelper {
    private final HolderGetter<Item> items;
    private final RecipeOutput output;

    UFarmersDelightRecipeGenerator(Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements, RecipeOutput exporter) {
        super(recipes, advancements);
        output = exporter;
        items = registries.lookupOrThrow(Registries.ITEM);
    }

    @Override
    public void buildRecipes() {
     // unwaxing
        UBlockFamilies.WAXED_ZAP.getVariants().forEach((variant, waxed) -> {
            if (variant == Variant.WALL_SIGN) return;
            var unwaxed = UBlockFamilies.ZAP.get(variant);
            CuttingBoardRecipeJsonBuilder.create(unwaxed, "axe_strip")
                .input(waxed).criterion(getHasName(waxed), has(waxed))
                .result(unwaxed)
                .result(Items.HONEYCOMB)
                .sound(SoundEvents.AXE_WAX_OFF.value())
                .offerTo(output, recipeId(getItemName(unwaxed) + "_from_waxed"));
        });
        List.of(UBlockFamilies.ZAP, UBlockFamilies.PALM).forEach(family -> {
            family.getVariants().forEach((variant, block) -> {
                if (variant == Variant.WALL_SIGN || variant == Variant.WALL_HANGING_SIGN) return;
                CuttingBoardRecipeJsonBuilder.create(family.getBaseBlock(), "axe_strip")
                    .input(block).criterion(getHasName(block), has(block))
                    .result(family.getBaseBlock())
                    .sound(SoundEvents.AXE_STRIP.value())
                    .offerTo(output, recipeId(getItemName(block)));
            });
        });
        Map.of(
                UBlocks.PALM_LOG, UBlocks.STRIPPED_PALM_LOG,
                UBlocks.PALM_WOOD, UBlocks.STRIPPED_PALM_WOOD,
                UBlocks.ZAP_LOG, UBlocks.STRIPPED_ZAP_LOG,
                UBlocks.ZAP_WOOD, UBlocks.STRIPPED_ZAP_WOOD
        ).forEach((unstripped, stripped) -> {
            CuttingBoardRecipeJsonBuilder.create(stripped, "axe_strip")
                .input(unstripped).criterion(getHasName(unstripped), has(unstripped))
                .sound(SoundEvents.AXE_STRIP.value())
                .result(stripped)
                .result(Identifier.parse("farmersdelight:tree_bark"))
                .offerTo(output, recipeId(getConversionRecipeName(stripped, unstripped)));
        });
        Map.of(
                UBlocks.GOLDEN_OAK_LOG, UBlocks.STRIPPED_GOLDEN_OAK_LOG,
                UBlocks.GOLDEN_OAK_WOOD, UBlocks.STRIPPED_GOLDEN_OAK_WOOD
        ).forEach((unstripped, stripped) -> {
            CuttingBoardRecipeJsonBuilder.create(stripped, "axe_strip")
                .input(unstripped).criterion(getHasName(unstripped), has(unstripped))
                .sound(SoundEvents.AXE_STRIP.value())
                .result(stripped)
                .result(Items.GOLD_NUGGET, 8)
                .offerTo(output, recipeId(getConversionRecipeName(stripped, unstripped)));
        });

        CuttingBoardRecipeJsonBuilder.create(UItems.HAY_FRIES, "axe_dig")
                .input(Blocks.HAY_BLOCK).criterion(getHasName(Blocks.HAY_BLOCK), has(Blocks.HAY_BLOCK))
                .sound(SoundEvents.AXE_SCRAPE.value())
                .result(UItems.HAY_FRIES, 9)
                .offerTo(output);

        CuttingBoardRecipeJsonBuilder.create(UItems.APPLE_PIE_SLICE, Ingredient.of(items.getOrThrow(UConventionalTags.Items.TOOL_KNIVES)))
            .input(UBlocks.APPLE_PIE).criterion(getHasName(UBlocks.APPLE_PIE), has(UBlocks.APPLE_PIE))
            .sound(USounds.BLOCK_PIE_SLICE)
            .result(UItems.APPLE_PIE_SLICE, 4)
            .offerTo(output);
    }

    public void offerSpell(RecipeOutput exporter, ItemLike gemstone, SpellType<?> output, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .traits(traits)
            .offerTo(exporter);
    }

    public void offerSpellFromSpell(RecipeOutput exporter, ItemLike gemstone, SpellType<?> output, SpellType<?> input, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .input(gemstone, input)
            .traits(traits)
            .offerTo(exporter);
    }

    public void offerSpellFromTwoSpells(RecipeOutput exporter, ItemLike gemstone, SpellType<?> output, SpellType<?> input1, SpellType<?> input2, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .input(gemstone, input1)
            .input(gemstone, input2)
            .traits(traits)
            .offerTo(exporter);
    }

    public static ResourceKey<Recipe<?>> recipeId(String key) {
        return ResourceKey.create(Registries.RECIPE, Unicopia.id(key));
    }

}
