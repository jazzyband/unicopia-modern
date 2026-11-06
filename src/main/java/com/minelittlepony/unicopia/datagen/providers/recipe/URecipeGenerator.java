package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.ItemFamilies;
import com.minelittlepony.unicopia.datagen.UBlockFamilies;
import com.minelittlepony.unicopia.datagen.providers.recipe.BedSheetPatternRecipeBuilder.PatternTemplate;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.GlowingRecipe;
import com.minelittlepony.unicopia.recipe.JarExtractRecipe;
import com.minelittlepony.unicopia.recipe.JarInsertRecipe;
import com.minelittlepony.unicopia.server.world.UTreeGen;
import com.mojang.datafixers.util.Either;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class URecipeGenerator extends RecipeProvider implements CraftingMaterialHelper {
    private static final List<Item> WOOLS = List.of(Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.BLACK).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.BLUE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.BROWN).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.CYAN).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.GRAY).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.GREEN).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.LIGHT_GRAY).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.LIME).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.MAGENTA).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.ORANGE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.PINK).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.PURPLE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.RED).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.YELLOW).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE).asItem());

    private final HolderGetter<Item> items;
    private final RecipeOutput output;

    private final UMagicRecipeGenerator magicRecipes;
    private final UFarmersDelightRecipeGenerator farmersDelightRecipes;

    private void oreSmelting(List<ItemLike> inputs, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group) {
        super.oreSmelting(inputs, category, category == RecipeCategory.FOOD ? CookingBookCategory.FOOD : CookingBookCategory.MISC,
                result, experience, cookingTime, group);
    }

    URecipeGenerator(Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements,
            Function<RecipeOutput, RecipeOutput> exporter, Function<RecipeOutput, RecipeOutput> farmersDelightExporter) {
        super(recipes, advancements);
        // Recipe providers run before item component data is bound in 26.3.
        // Item stacks used only to serialize recipe outputs need a temporary base map.
        BuiltInRegistries.ITEM.stream().map(Item::builtInRegistryHolder).forEach(holder -> {
            if (!holder.areComponentsBound()) {
                holder.bindComponents(DataComponentMap.EMPTY);
            }
        });
        output = exporter.apply(super.output);
        items = registries.lookupOrThrow(Registries.ITEM);
        this.magicRecipes = new UMagicRecipeGenerator(recipes, advancements, output);
        this.farmersDelightRecipes = new UFarmersDelightRecipeGenerator(registries, recipes, advancements, farmersDelightExporter.apply(super.output));
    }

    @Override
    public void buildRecipes() {
        offerJarRecipes();
        offerWoodBlocksRecipes();
        offerChitinBlocksRecipes();
        offerCloudRecipes();
        offerFoodRecipes();
        offerGemstoneAndMagicRecipes();
        offerSeaponyRecipes();
        offerEarthPonyRecipes();

        // beds
        createCustomBedRecipe(UItems.CLOUD_BED, Either.left(UBlocks.DENSE_CLOUD), Either.left(UBlocks.CLOUD_PLANKS)).save(this.output);
        createCustomBedRecipe(UItems.CLOTH_BED, Either.right(ItemTags.WOOL), Either.right(ItemTags.LOGS)).save(this.output);
        offerBedSheetRecipes();

        // sunglasses
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, UItems.SUNGLASSES)
            .define('#', ConventionalItemTags.GLASS_BLOCKS).unlockedBy("has_glass_block", has(ConventionalItemTags.GLASS_BLOCKS))
            .pattern("##")
            .save(this.output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, UItems.SUNGLASSES)
            .requires(ConventionalItemTags.GLASS_BLOCKS)
            .requires(UItems.SUNGLASSES).unlockedBy("has_broken_sunglasses", has(UItems.BROKEN_SUNGLASSES))
            .save(this.output, recipeId(getConversionRecipeName(UItems.SUNGLASSES, UItems.BROKEN_SUNGLASSES)));

        magicRecipes.buildRecipes();
        farmersDelightRecipes.buildRecipes();
    }

    private void offerJarRecipes() {
        SpecialRecipeBuilder.special(() -> new JarExtractRecipe(CraftingBookCategory.MISC)).save(this.output, recipeId("empty_jar_from_filled_jar"));
        SpecialRecipeBuilder.special(() -> new JarInsertRecipe(CraftingBookCategory.MISC)).save(this.output, recipeId("filled_jar"));
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, UItems.EMPTY_JAR, 7)
            .define('#', ItemTags.PLANKS)
            .define('*', ConventionalItemTags.GLASS_BLOCKS).unlockedBy("has_glass", has(ConventionalItemTags.GLASS_BLOCKS))
            .pattern("*#*")
            .pattern("* *")
            .pattern("***")
            .save(this.output);
    }

    private void offerCloudRecipes() {
        offerShapelessRecipe(UItems.CLOUD_LUMP, UTags.Items.CLOUD_JARS, "cloud", 4);
        generateRecipes(UBlockFamilies.CLOUD, FeatureFlagSet.of(FeatureFlags.VANILLA));
        offer2x3Recipe(UBlocks.CLOUD_PILLAR, UBlocks.CLOUD, "pillar");
        twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD, UItems.CLOUD_LUMP);
        polished(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANKS, UBlocks.CLOUD);
        generateRecipes(UBlockFamilies.CLOUD_PLANKS, FeatureFlagSet.of(FeatureFlags.VANILLA));
        offerChestRecipe(UBlocks.CLOUD_CHEST, UBlocks.CLOUD_PLANKS);

        twoByTwoPacker(RecipeCategory.DECORATIONS, UBlocks.SHAPING_BENCH, UBlocks.DENSE_CLOUD);
        generateRecipes(UBlockFamilies.CLOUD_BRICKS, FeatureFlagSet.of(FeatureFlags.VANILLA));

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CARVED_CLOUD, UBlocks.CLOUD);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.ETCHED_CLOUD, UBlocks.CLOUD);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_BRICKS, UBlocks.CLOUD);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANKS, UBlocks.CLOUD);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PILLAR, UBlocks.CLOUD);

        // TODO: Cut Cloud, Smooth Cloud, Polished Cloud, Raked Cloud

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_SLAB, UBlocks.CLOUD, 2);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_STAIRS, UBlocks.CLOUD);

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_BRICK_SLAB, UBlocks.CLOUD_BRICKS, 2);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_BRICK_STAIRS, UBlocks.CLOUD_BRICKS);

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANK_SLAB, UBlocks.CLOUD_PLANKS, 2);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANK_STAIRS, UBlocks.CLOUD_PLANKS);

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.DENSE_CLOUD_SLAB, UBlocks.DENSE_CLOUD, 2);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.DENSE_CLOUD_STAIRS, UBlocks.DENSE_CLOUD);

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.ETCHED_CLOUD_SLAB, UBlocks.ETCHED_CLOUD, 2);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.ETCHED_CLOUD_STAIRS, UBlocks.ETCHED_CLOUD);

        offerCompactingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.DENSE_CLOUD, UBlocks.CLOUD, 4);
        generateRecipes(UBlockFamilies.DENSE_CLOUD, FeatureFlagSet.of(FeatureFlags.VANILLA));
        offer2x3Recipe(UBlocks.CLOUD_DOOR, UBlocks.DENSE_CLOUD, "door");

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, UBlocks.UNSTABLE_CLOUD, 8)
            .requires(UBlocks.CLOUD, 8)
            .requires(Ingredient.of(UItems.LIGHTNING_JAR, UItems.ZAP_APPLE_JAM_JAR))
            .unlockedBy("has_lightning_jar", has(UItems.LIGHTNING_JAR))
            .unlockedBy("has_zap_jar", has(UItems.ZAP_APPLE_JAM_JAR))
            .save(this.output);
    }

    private void offerWoodBlocksRecipes() {
        // palm wood
        generateRecipes(UBlockFamilies.PALM, FeatureFlagSet.of(FeatureFlags.VANILLA));
        planksFromLogs(UBlocks.PALM_PLANKS, UTags.Items.PALM_LOGS, 4);
        woodFromLogs(UBlocks.PALM_WOOD, UBlocks.PALM_LOG);
        woodFromLogs(UBlocks.STRIPPED_PALM_WOOD, UBlocks.STRIPPED_PALM_LOG);
        woodenBoat(UItems.PALM_BOAT, UBlocks.PALM_PLANKS);
        chestBoat(UItems.PALM_CHEST_BOAT, UItems.PALM_BOAT);

        // zap wood
        generateRecipes(UBlockFamilies.ZAP, FeatureFlagSet.of(FeatureFlags.VANILLA));
        planksFromLogs(UBlocks.ZAP_PLANKS, UTags.Items.ZAP_LOGS, 4);
        woodFromLogs(UBlocks.ZAP_WOOD, UBlocks.ZAP_LOG);
        woodFromLogs(UBlocks.STRIPPED_ZAP_WOOD, UBlocks.STRIPPED_ZAP_LOG);

        // waxed zap wood
        planksFromLogs(UBlocks.WAXED_ZAP_PLANKS, UTags.Items.WAXED_ZAP_LOGS, 4);
        woodFromLogs(UBlocks.WAXED_ZAP_WOOD, UBlocks.WAXED_ZAP_LOG);
        generateRecipes(UBlockFamilies.WAXED_ZAP, FeatureFlagSet.of(FeatureFlags.VANILLA));
        woodFromLogs(UBlocks.WAXED_STRIPPED_ZAP_WOOD, UBlocks.WAXED_STRIPPED_ZAP_LOG);

        // golden oak wood
        generateRecipes(UBlockFamilies.GOLDEN_OAK, FeatureFlagSet.of(FeatureFlags.VANILLA));
        planksFromLogs(UBlocks.GOLDEN_OAK_PLANKS, UTags.Items.GOLDEN_OAK_LOGS, 4);
        woodFromLogs(UBlocks.GOLDEN_OAK_WOOD, UBlocks.GOLDEN_OAK_LOG);
        woodFromLogs(UBlocks.STRIPPED_GOLDEN_OAK_WOOD, UBlocks.STRIPPED_GOLDEN_OAK_LOG);

        oreSmelting(List.of(UBlocks.GOLDEN_OAK_LOG, UBlocks.GOLDEN_OAK_WOOD, UBlocks.STRIPPED_GOLDEN_OAK_LOG, UBlocks.STRIPPED_GOLDEN_OAK_WOOD, UItems.GOLDEN_STICK), RecipeCategory.FOOD, Items.GOLD_NUGGET, 20, 200, "gold_nugget");

        offerWaxingRecipes();

        // other doors
        offer2x3Recipe(UBlocks.CRYSTAL_DOOR, UItems.CRYSTAL_SHARD, "door");
        offerStableDoorRecipe(UBlocks.STABLE_DOOR, Either.right(ItemTags.PLANKS), UItems.ROCK_CANDY);
        offerStableDoorRecipe(UBlocks.DARK_OAK_DOOR, Either.right(ItemTags.PLANKS), UItems.ROCK);
    }

    private void offerChitinBlocksRecipes() {
        nineBlockStorageRecipes(RecipeCategory.BUILDING_BLOCKS, UItems.CARAPACE, RecipeCategory.BUILDING_BLOCKS, UBlocks.CHITIN);
        polished(RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN, UBlocks.CHITIN);

        generateRecipes(UBlockFamilies.CHISELED_CHITIN, FeatureFlagSet.of(FeatureFlags.VANILLA));
        generateRecipes(UBlockFamilies.POLISHED_CHITIN, FeatureFlagSet.of(FeatureFlags.VANILLA));
        offerHiveRecipe(UBlocks.HIVE, UBlocks.CHITIN, UBlocks.MYSTERIOUS_EGG);
        offerHullRecipe(UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHISELLED_CHITIN, UBlocks.CHITIN);
        offerHullRecipe(UBlocks.POLISHED_CHITIN_HULL, UBlocks.POLISHED_CHITIN, UBlocks.CHITIN);
        offerSpikesRecipe(UBlocks.CHITIN_SPIKES, UBlocks.CHITIN);

        stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, UBlocks.POLISHED_CHITIN, UBlocks.CHISELLED_CHITIN);
        stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHISELLED_CHITIN);
        stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN_SLAB, UBlocks.CHISELLED_CHITIN, 2);
        stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN_STAIRS, UBlocks.CHISELLED_CHITIN);
        stonecutterResultFromBase(RecipeCategory.DECORATIONS, UBlocks.CHISELLED_CHITIN_WALL, UBlocks.CHISELLED_CHITIN);

        stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, UBlocks.POLISHED_CHITIN_HULL, UBlocks.POLISHED_CHITIN);
        stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, UBlocks.POLISHED_CHITIN_SLAB, UBlocks.POLISHED_CHITIN, 2);
        stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, UBlocks.POLISHED_CHITIN_STAIRS, UBlocks.POLISHED_CHITIN);
        stonecutterResultFromBase(RecipeCategory.DECORATIONS, UBlocks.POLISHED_CHITIN_WALL, UBlocks.POLISHED_CHITIN);
    }

    private void offerGemstoneAndMagicRecipes() {
        oneToOneConversionRecipe(UItems.CRYSTAL_SHARD, Items.DIAMOND, "crystal_shard", 6);
        oneToOneConversionRecipe(UItems.CRYSTAL_SHARD, Items.AMETHYST_SHARD, "crystal_shard", 3);
        twoByTwoPacker(RecipeCategory.MISC, UItems.GEMSTONE, UItems.CRYSTAL_SHARD);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, UItems.SPELLBOOK)
            .requires(Items.BOOK).unlockedBy("has_book", has(Items.BOOK))
            .requires(UItems.GEMSTONE).unlockedBy("has_gemstone", has(UItems.GEMSTONE))
            .save(this.output);

        // crystal heart
        ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS, UItems.CRYSTAL_HEART)
            .define('#', UItems.CRYSTAL_SHARD).unlockedBy("has_crystal_shard", has(UItems.CRYSTAL_SHARD))
            .pattern("# #")
            .pattern("###")
            .pattern(" # ")
            .save(this.output);

        // pegasus amulet
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, UItems.GOLDEN_FEATHER)
            .define('*', Items.GOLD_NUGGET).unlockedBy("has_nugget", has(Items.GOLD_NUGGET))
            .define('#', UTags.Items.MAGIC_FEATHERS).unlockedBy("has_feather", has(UTags.Items.MAGIC_FEATHERS))
            .pattern("***")
            .pattern("*#*")
            .pattern("***")
            .save(this.output);
        threeByThreePacker(RecipeCategory.COMBAT, UItems.GOLDEN_WING, UItems.GOLDEN_FEATHER);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.TOOLS, UItems.PEGASUS_AMULET)
            .define('*', UItems.GOLDEN_WING).unlockedBy("has_wing", has(UItems.GOLDEN_WING))
            .define('#', UItems.GEMSTONE).unlockedBy("has_gemstone", has(UItems.GEMSTONE))
            .pattern("*#*")
            .save(this.output);

        // friendship bracelet
        ShapedRecipeBuilder.shaped(items, RecipeCategory.TOOLS, UItems.FRIENDSHIP_BRACELET)
            .define('*', Items.STRING)
            .define('#', Items.LEATHER).unlockedBy(getHasName(Items.LEATHER), has(Items.LEATHER))
            .pattern("*#*")
            .pattern("# #")
            .pattern("*#*")
            .save(this.output);
        SpecialRecipeBuilder.special(() -> new GlowingRecipe(CraftingBookCategory.MISC)).save(this.output, recipeId("friendship_bracelet_glowing"));

        // magic staff
        SpellShapedRecipeJsonBuilder.create(items, RecipeCategory.TOOLS, UItems.MAGIC_STAFF)
            .withSpellFrom(UItems.GEMSTONE)
            .input('*', UItems.GEMSTONE).unlockedBy(getHasName(UItems.GEMSTONE), has(UItems.GEMSTONE))
            .input('/', ConventionalItemTags.WOODEN_RODS).unlockedBy(getHasName(Items.STICK), has(ConventionalItemTags.WOODEN_RODS))
            .pattern("  *")
            .pattern(" / ")
            .pattern("/  ")
            .save(this.output);
        // meadowbrook's staff
        oneToOneConversionRecipe(Items.STICK, UItems.MEADOWBROOKS_STAFF, "stick", 2);
    }

    private void offerFoodRecipes() {
        oneToOneConversionRecipe(UItems.PINEAPPLE_CROWN, UItems.PINEAPPLE, "seeds", 1);
        oneToOneConversionRecipe(UItems.SWEET_APPLE_SEEDS, UItems.SWEET_APPLE, "seeds", 3);
        oneToOneConversionRecipe(UItems.SOUR_APPLE_SEEDS, UItems.SOUR_APPLE, "seeds", 3);
        oneToOneConversionRecipe(UItems.GREEN_APPLE_SEEDS, UItems.GREEN_APPLE, "seeds", 3);
        oneToOneConversionRecipe(UItems.GOLDEN_OAK_SEEDS, Items.GOLDEN_APPLE, "seeds", 1);
        offerPieRecipe(UItems.APPLE_PIE, UItems.APPLE_PIE_SLICE, Items.WHEAT, UTags.Items.FRESH_APPLES);

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.ROCK_STEW)
            .requires(UItems.ROCK, 3).unlockedBy(getHasName(UItems.ROCK), has(UItems.ROCK))
            .requires(Items.BOWL)
            .save(this.output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.BOWL_OF_NUTS)
            .requires(UItems.ACORN, 3).unlockedBy(getHasName(UItems.ACORN), has(UItems.ACORN))
            .requires(Items.BOWL)
            .save(this.output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.OATMEAL_COOKIE)
            .requires(UItems.OATS, 2).unlockedBy(getHasName(UItems.OATS), has(UItems.OATS))
            .save(this.output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.CHOCOLATE_OATMEAL_COOKIE)
            .requires(UItems.OATS, 2)
            .requires(Items.COCOA_BEANS).unlockedBy(getHasName(Items.COCOA_BEANS), has(Items.COCOA_BEANS))
            .save(this.output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.PINECONE_COOKIE)
            .requires(UItems.PINECONE).unlockedBy(getHasName(UItems.PINECONE), has(UItems.PINECONE))
            .requires(Items.WHEAT, 2)
            .save(this.output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.SCONE)
            .requires(UItems.OATS).unlockedBy(getHasName(UItems.OATS), has(UItems.OATS))
            .requires(Items.WHEAT, 2)
            .save(this.output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.ROCK_CANDY, 3)
            .requires(Items.SUGAR, 6).unlockedBy(getHasName(Items.SUGAR), has(Items.SUGAR))
            .requires(UItems.PEBBLES, 3)
            .save(this.output);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.FOOD, Items.BREAD)
            .define('#', UItems.OATS).unlockedBy("has_oats", has(UItems.OATS))
            .pattern("###")
            .save(this.output, recipeId(getConversionRecipeName(Items.BREAD, UItems.OATS)));
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.JUICE)
            .requires(Ingredient.of(items.getOrThrow(UTags.Items.FRESH_APPLES)), 6).unlockedBy(getHasName(Items.APPLE), has(UTags.Items.FRESH_APPLES))
            .requires(Items.GLASS_BOTTLE)
            .group("juice")
            .save(this.output);
        appendIngredients(ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.MUFFIN), Items.SUGAR, Items.EGG, Items.POTATO, UItems.JUICE, UItems.WHEAT_WORMS).save(this.output);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, UItems.MUG)
            .define('*', Items.IRON_NUGGET).unlockedBy(getHasName(Items.IRON_NUGGET), has(Items.IRON_NUGGET))
            .define('#', ConventionalItemTags.WOODEN_RODS).unlockedBy(getHasName(Items.STICK), has(ConventionalItemTags.WOODEN_RODS))
            .pattern("# #")
            .pattern("* *")
            .pattern(" # ")
            .save(this.output);
        appendIngredients(ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.CIDER), UItems.BURNED_JUICE, UItems.MUG)
            .requires(Ingredient.of(items.getOrThrow(UTags.Items.FRESH_APPLES))).unlockedBy(getHasName(Items.APPLE), has(UTags.Items.FRESH_APPLES))
            .save(this.output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.FOOD, UItems.HAY_FRIES)
            .define('#', UItems.OATS).unlockedBy(getHasName(UItems.OATS), has(UItems.OATS))
            .pattern("#")
            .pattern("#")
            .pattern("#")
            .save(this.output);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.FOOD, UItems.HAY_BURGER)
            .define('~', Items.BREAD).unlockedBy(getHasName(Items.BREAD), has(Items.BREAD))
            .define('#', UItems.OATS).unlockedBy(getHasName(UItems.OATS), has(UItems.OATS))
            .pattern(" # ")
            .pattern("~~~")
            .pattern(" # ")
            .save(this.output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.FOOD, UItems.DAFFODIL_DAISY_SANDWICH)
            .define('#', Items.BREAD).unlockedBy(getHasName(Items.BREAD), has(Items.BREAD))
            .define('~', net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SMALL_FLOWERS).unlockedBy("has_flower", has(net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SMALL_FLOWERS))
            .pattern(" # ")
            .pattern("~~~")
            .pattern(" # ")
            .save(this.output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.FOOD, UItems.HORSE_SHOE_FRIES, 15)
            .define('#', Items.BAKED_POTATO).unlockedBy(getHasName(Items.BAKED_POTATO), has(Items.BAKED_POTATO))
            .pattern("# #")
            .pattern("# #")
            .pattern(" # ")
            .save(this.output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.OATMEAL)
            .requires(UItems.OATS, 3).unlockedBy(getHasName(UItems.OATS), has(UItems.OATS))
            .requires(ConventionalItemTags.MILK_BUCKETS)
            .requires(Items.BOWL)
            .save(this.output);

        oreSmelting(List.of(UItems.JUICE), RecipeCategory.FOOD, UItems.BURNED_JUICE, 0, 100, "juice");
        oreSmelting(List.of(Items.BREAD), RecipeCategory.FOOD, UItems.TOAST, 0.2F, 430, "bread");
        oreSmelting(List.of(UItems.TOAST), RecipeCategory.FOOD, UItems.BURNED_TOAST, 0.2F, 30, "bread");
        oreSmelting(List.of(UItems.BURNED_JUICE, UItems.BURNED_TOAST), RecipeCategory.FOOD, Items.CHARCOAL, 1, 20, "coal");
        oreSmelting(List.of(UItems.HAY_FRIES), RecipeCategory.FOOD, UItems.CRISPY_HAY_FRIES, 1F, 25, "hay_fries");
        oreSmelting(List.of(UItems.ZAP_APPLE), RecipeCategory.FOOD, UItems.COOKED_ZAP_APPLE, 1.2F, 430, "zap_apple");
        oreSmelting(List.of(Items.TROPICAL_FISH), RecipeCategory.FOOD, UItems.COOKED_TROPICAL_FISH, 1.2F, 230, "fish");
        oreSmelting(List.of(Items.PUFFERFISH), RecipeCategory.FOOD, UItems.COOKED_PUFFERFISH, 1.2F, 230, "fish");
        oreSmelting(List.of(Items.AXOLOTL_BUCKET), RecipeCategory.FOOD, UItems.FRIED_AXOLOTL, 2.2F, 230, "fried_axolotl");
        oreSmelting(List.of(UItems.FROG_LEGS), RecipeCategory.FOOD, UItems.COOKED_FROG_LEGS, 2.2F, 10, "frog_legs");
        oreSmelting(List.of(UBlocks.MYSTERIOUS_EGG.asItem()), RecipeCategory.FOOD, UItems.GREEN_FRIED_EGG, 3.8F, 630, "fried_egg");
        cookRecipes("smoking", SmokingRecipe::new, 100);
        cookRecipes("campfire_cooking", CampfireCookingRecipe::new, 600);

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.ZAP_APPLE_JAM_JAR)
            .requires(UItems.COOKED_ZAP_APPLE, 6).unlockedBy(getHasName(UItems.COOKED_ZAP_APPLE), has(UItems.COOKED_ZAP_APPLE))
            .requires(UItems.EMPTY_JAR)
            .save(this.output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.JAM_TOAST, 8)
            .requires(UItems.ZAP_APPLE_JAM_JAR).unlockedBy(getHasName(UItems.ZAP_APPLE_JAM_JAR), has(UItems.ZAP_APPLE_JAM_JAR))
            .requires(UItems.TOAST, 8)
            .save(this.output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, UItems.CANDIED_APPLE)
            .requires(ConventionalItemTags.WOODEN_RODS)
            .requires(UTags.Items.FRESH_APPLES).unlockedBy(getHasName(Items.APPLE), has(UTags.Items.FRESH_APPLES))
            .requires(Items.SUGAR, 4)
            .save(this.output);

        // trick apples
        offerTrickRecipe(UItems.GREEN_APPLE, Items.DYE.green());
        offerTrickRecipe(Items.APPLE, Items.DYE.red());
        offerTrickRecipe(UItems.SOUR_APPLE, Items.DYE.yellow());
        offerTrickRecipe(UItems.SWEET_APPLE, Items.DYE.orange());
        offerTrickRecipe(UItems.ROTTEN_APPLE, Items.ROTTEN_FLESH);
        offerTrickRecipe(UItems.COOKED_ZAP_APPLE, Items.SPIDER_EYE);
        offerTrickRecipe(Items.GOLDEN_APPLE, Items.GOLD_NUGGET);
        offerTrickRecipe(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLD_INGOT);
        offerTrickRecipe(UItems.MANGO, Items.DYE.lime());
        offerTrickRecipe(UItems.PINEAPPLE, UItems.PINEAPPLE_CROWN);
        offerTrickRecipe(UItems.HORSE_SHOE_FRIES, UItems.IRON_HORSE_SHOE);
        offerTrickRecipe(UItems.MUFFIN, UItems.ROCK);

    }

    @Override
    public <T extends AbstractCookingRecipe> void cookRecipes(String cooker, AbstractCookingRecipe.Factory<T> recipeFactory, int cookingTime) {
        simpleCookingRecipe(cooker, recipeFactory, cookingTime / 3, UItems.JUICE, UItems.BURNED_JUICE, 0);
        simpleCookingRecipe(cooker, recipeFactory, cookingTime, Items.BREAD, UItems.TOAST, 0.2F);
        simpleCookingRecipe(cooker, recipeFactory, cookingTime, UItems.TOAST, UItems.BURNED_TOAST, 0.2F);
        simpleCookingRecipe(cooker, recipeFactory, cookingTime / 2, UItems.BURNED_TOAST, Items.CHARCOAL, 1);
        simpleCookingRecipe(cooker, recipeFactory, cookingTime, UItems.HAY_FRIES, UItems.CRISPY_HAY_FRIES, 1F);
        simpleCookingRecipe(cooker, recipeFactory, cookingTime, UItems.ZAP_APPLE, UItems.COOKED_ZAP_APPLE, 0.6F);
        simpleCookingRecipe(cooker, recipeFactory, cookingTime, Items.TROPICAL_FISH, UItems.COOKED_TROPICAL_FISH, 0.35F);
        simpleCookingRecipe(cooker, recipeFactory, cookingTime, Items.PUFFERFISH, UItems.COOKED_PUFFERFISH, 1.2F);
        simpleCookingRecipe(cooker, recipeFactory, cookingTime + 30, Items.AXOLOTL_BUCKET, UItems.FRIED_AXOLOTL, 2.2F);
        simpleCookingRecipe(cooker, recipeFactory, cookingTime, UItems.FROG_LEGS, UItems.COOKED_FROG_LEGS, 2.2F);
        simpleCookingRecipe(cooker, recipeFactory, cookingTime + 50, UBlocks.MYSTERIOUS_EGG, UItems.GREEN_FRIED_EGG, 3.8F);
    }

    public void offerTrickRecipe(ItemLike output, ItemLike input) {
        TrickCraftingRecipeJsonBuilder.create(RecipeCategory.FOOD, output)
            .input(UItems.ZAP_APPLE).unlockedBy(getHasName(UItems.ZAP_APPLE), has(UItems.ZAP_APPLE))
            .input(input)
            .save(this.output, recipeId(getConversionRecipeName(output, input) + "_trick"));
    }

    private void offerSeaponyRecipes() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, UItems.SHELLY)
            .define('C', UItems.CLAM_SHELL).unlockedBy("has_clam_shell", has(UItems.CLAM_SHELL))
            .define('o', UItems.ROCK_CANDY)
            .pattern("o o")
            .pattern(" C ")
            .save(this.output);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, UItems.PEARL_NECKLACE)
            .define('#', UTags.Items.SHELLS).unlockedBy("has_shell", has(UTags.Items.SHELLS))
            .define('~', Items.STRING)
            .pattern("# #")
            .pattern("# #")
            .pattern("~#~")
            .save(this.output);
    }

    private void offerEarthPonyRecipes() {
        Arrays.stream(ItemFamilies.BASKETS).forEach(basket -> offerBasketRecipe(basket, getMaterial(basket, "_basket", "_planks")));
        Arrays.stream(ItemFamilies.HORSE_SHOES).forEach(horseshoe -> offerHorseshoeRecipe(horseshoe, getMaterial(horseshoe, "_horse_shoe", "_ingot")));
        Arrays.stream(ItemFamilies.POLEARMS).forEach(polearm -> {
            if (polearm == UItems.NETHERITE_POLEARM) {
                netheriteSmithing(UItems.DIAMOND_POLEARM, RecipeCategory.TOOLS, UItems.NETHERITE_POLEARM);
            } else {
                offerPolearmRecipe(polearm, getMaterial(polearm, "_polearm", "_ingot"));
            }
        });
        // weather vane
        offerWeatherVaneRecipe(UBlocks.WEATHER_VANE, Items.IRON_NUGGET);

        // Giant balloons
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, UItems.GIANT_BALLOON)
            .define('-', ItemTags.WOOL_CARPETS).unlockedBy("has_carpet", has(ItemTags.WOOL_CARPETS))
            .define('#', ItemTags.WOOL).unlockedBy("has_wool", has(ItemTags.WOOL))
            .pattern("---")
            .pattern("# #")
            .pattern("---")
            .save(this.output);

        // worms
        nineBlockStorageRecipes(RecipeCategory.BUILDING_BLOCKS, UItems.WHEAT_WORMS, RecipeCategory.BUILDING_BLOCKS, UBlocks.WORM_BLOCK);
        // fishing
        ItemConversionShapedRecipeBuilder.create(items, RecipeCategory.MISC, Items.FISHING_ROD, UItems.BAITED_FISHING_ROD)
            .input('#', Items.FISHING_ROD).unlockedBy(getHasName(Items.FISHING_ROD), has(Items.FISHING_ROD))
            .input('*', UItems.WHEAT_WORMS).unlockedBy("has_wheat_worms", has(UItems.WHEAT_WORMS))
            .pattern("# ")
            .pattern(" *")
            .group("fishing_rod")
            .save(this.output);

        // utility
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.DIRT)
            .define('*', UItems.WHEAT_WORMS).unlockedBy("has_wheat_worms", has(UItems.WHEAT_WORMS))
            .define('#', ItemTags.SAND).unlockedBy("has_sand", has(ItemTags.SAND))
            .pattern("*#")
            .pattern("#*")
            .save(this.output, recipeId(getConversionRecipeName(Items.DIRT, UItems.WHEAT_WORMS)));

        offerShapelessRecipe(Items.BONE_MEAL, UTags.Items.SHELLS, "bonemeal", 3);

        // pegasus feathers for non pegasi
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, UItems.PEGASUS_FEATHER)
            .define('*', Items.GHAST_TEAR).unlockedBy("has_ghast_tear", has(Items.GHAST_TEAR))
            .define('#', UItems.GRYPHON_FEATHER).unlockedBy("has_feather", has(UItems.GRYPHON_FEATHER))
            .pattern("***")
            .pattern("*#*")
            .pattern("***")
            .save(this.output);

        twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, Items.COBBLESTONE, UItems.ROCK);
        nineBlockStorageRecipesRecipesWithCustomUnpacking(RecipeCategory.MISC, UItems.PEBBLES, RecipeCategory.BUILDING_BLOCKS, Blocks.GRAVEL, getConversionRecipeName(UItems.PEBBLES, Blocks.GRAVEL), "pebbles");
        oneToOneConversionRecipe(UItems.PEBBLES, Blocks.SUSPICIOUS_GRAVEL, "pebbles", 9);
        oreSmelting(List.of(UItems.GOLDEN_OAK_SEEDS, UItems.GOLDEN_FEATHER), RecipeCategory.MISC, Items.GOLD_NUGGET, 3F, 10, "gold_nugget");

        offerGrowing(UBlocks.CURING_JOKE, Blocks.LAPIS_BLOCK, Blocks.CORNFLOWER);
        offerGrowing(UBlocks.GOLD_ROOT, Blocks.RAW_GOLD_BLOCK, Blocks.CARROTS);
        offerGrowing(UTreeGen.GOLDEN_OAK_TREE.sapling().get(), Blocks.RAW_GOLD_BLOCK, Blocks.OAK_SAPLING);
        offerGrowing(UBlocks.PLUNDER_VINE_BUD, Blocks.NETHERRACK, Blocks.WITHER_ROSE);
        offerGrowing(UTreeGen.ZAP_APPLE_TREE.sapling().get(), UBlocks.CHITIN, Blocks.DARK_OAK_SAPLING);
    }

    private ShapelessRecipeBuilder appendIngredients(ShapelessRecipeBuilder builder, ItemLike...ingredients) {
        for (ItemLike ingredient : ingredients) {
            builder.requires(ingredient).unlockedBy(getHasName(ingredient), has(ingredient));
        }
        return builder;
    }

    public void offerShapelessRecipe(ItemLike output, TagKey<Item> input, @Nullable String group, int outputCount) {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, output, outputCount)
            .requires(input).unlockedBy(hasTag(input), has(input))
            .group(group)
            .save(this.output, recipeId(getItemName(output) + "_from_" + input.location().getPath()));
    }

    public void offerPieRecipe(ItemLike pie, ItemLike slice, ItemLike crust, TagKey<Item> filling) {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.FOOD, pie)
            .define('*', crust).unlockedBy("has_crust", has(crust))
            .define('#', filling).unlockedBy("has_filling", has(filling))
            .pattern("***")
            .pattern("###")
            .pattern("***")
            .save(this.output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, pie)
            .requires(slice, 4)
            .unlockedBy(getHasName(slice), has(slice))
            .save(this.output, getItemName(pie) + "_from_" + getItemName(slice));
    }

    public void offerBasketRecipe(ItemLike output, Either<ItemLike, TagKey<Item>> input) {
        input(ShapedRecipeBuilder.shaped(items, RecipeCategory.TRANSPORTATION, output), '#', input)
            .unlockedBy(hasEither(input), conditionsFromEither(input))
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .group("basket")
            .save(this.output);
    }

    public void offerHorseshoeRecipe(ItemLike output, Either<ItemLike, TagKey<Item>> input) {
        input(ShapedRecipeBuilder.shaped(items, RecipeCategory.COMBAT, output), '#', input)
            .unlockedBy(hasEither(input), conditionsFromEither(input))
            .pattern("# #")
            .pattern("# #")
            .pattern(" # ")
            .group("horseshoe")
            .save(this.output);
    }

    public void offerHiveRecipe(ItemLike output, ItemLike chitin, ItemLike egg) {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, output)
            .define('#', chitin)
            .define('o', egg).unlockedBy(getHasName(egg), has(egg))
            .pattern(" # ")
            .pattern("#o#")
            .pattern(" # ")
            .group("chitin")
            .save(this.output);
    }

    public void offerPolearmRecipe(ItemLike output, Either<ItemLike, TagKey<Item>> input) {
        input(ShapedRecipeBuilder.shaped(items, RecipeCategory.TOOLS, output), 'o', input).unlockedBy(hasEither(input), conditionsFromEither(input))
            .define('#', ConventionalItemTags.WOODEN_RODS)
            .define('s', ConventionalItemTags.STRINGS)
            .pattern("  o")
            .pattern(" #s")
            .pattern("#  ")
            .group("polearm")
            .save(this.output);
    }

    public void offerHullRecipe(ItemLike output, ItemLike outside, ItemLike inside) {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.BUILDING_BLOCKS, output, 4)
            .define('#', outside).unlockedBy(getHasName(outside), has(outside))
            .define('o', inside).unlockedBy(getHasName(inside), has(inside))
            .pattern("##")
            .pattern("oo")
            .group("hull")
            .save(this.output);
    }

    public void offerSpikesRecipe(ItemLike output, ItemLike input) {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS, output, 8)
            .define('#', input).unlockedBy(getHasName(input), has(input))
            .pattern(" # ")
            .pattern("###")
            .group("spikes")
            .save(this.output);
    }

    public void offerChestRecipe(ItemLike output, ItemLike input) {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS, output)
            .define('#', input)
            .pattern("###")
            .pattern("# #")
            .pattern("###")
            .unlockedBy("has_lots_of_items", CriteriaTriggers.INVENTORY_CHANGED.createCriterion(new InventoryChangeTrigger.TriggerInstance(
                    Optional.empty(),
                    new InventoryChangeTrigger.TriggerInstance.Slots(MinMaxBounds.Ints.atLeast(10), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY),
                    List.of())))
            .save(this.output);
    }

    public void offer2x3Recipe(ItemLike output, ItemLike input, String group) {
        doorBuilder(output, Ingredient.of(input))
            .unlockedBy(getHasName(input), has(input))
            .group(group)
            .save(this.output);
    }

    public void offerStableDoorRecipe(ItemLike output, Either<ItemLike, TagKey<Item>> body, ItemLike trim) {
        input(ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, output, 3), '#', body).unlockedBy(hasEither(body), conditionsFromEither(body))
            .define('*', trim).unlockedBy(getHasName(trim), has(trim))
            .pattern("*#*")
            .pattern("*#*")
            .pattern("*#*")
            .group("stable_door")
            .save(this.output);
    }

    public void offerWeatherVaneRecipe(ItemLike output, ItemLike input) {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS, output)
            .define('*', input).unlockedBy(getHasName(input), has(input))
            .pattern(" **")
            .pattern("** ")
            .pattern(" * ")
            .save(this.output);
    }

    public ShapedRecipeBuilder createCustomBedRecipe(ItemLike output, Either<ItemLike, TagKey<Item>> input, Either<ItemLike, TagKey<Item>> planks) {
        var builder = ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS, output);
        input(builder, '#', input).unlockedBy(hasEither(input), conditionsFromEither(input));
        return input(builder, 'X', planks)
            .pattern("###")
            .pattern("XXX")
            .group("bed");
    }

    private void offerBedSheetRecipes() {
        PatternTemplate.ONE_COLOR.offerWithoutConversion(this, items, output, UItems.KELP_BED_SHEETS, Items.KELP);
        WOOLS.forEach(wool -> PatternTemplate.ONE_COLOR.offerTo(this, items, output, getItem(Unicopia.id(BuiltInRegistries.ITEM.getKey(wool).getPath().replace("_wool", "_bed_sheets"))), wool));

        PatternTemplate.TWO_COLOR.offerTo(this, items, output, UItems.APPLE_BED_SHEETS, Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.GREEN).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.LIME).asItem());
        PatternTemplate.TWO_COLOR.offerTo(this, items, output, UItems.BARRED_BED_SHEETS, Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE).asItem());
        PatternTemplate.TWO_COLOR.offerTo(this, items, output, UItems.CHECKERED_BED_SHEETS, Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.GREEN).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.BROWN).asItem());
        PatternTemplate.THREE_COLOR.offerTo(this, items, output, UItems.RAINBOW_PWR_BED_SHEETS, Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.PINK).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.RED).asItem());
        PatternTemplate.THREE_COLOR.offerTo(this, items, output, UItems.RAINBOW_BPY_BED_SHEETS, Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.PINK).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.YELLOW).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE).asItem());
        PatternTemplate.THREE_COLOR.offerTo(this, items, output, UItems.RAINBOW_BPW_BED_SHEETS, Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.PINK).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE).asItem());
        PatternTemplate.FOUR_COLOR.offerTo(this, items, output, UItems.RAINBOW_PBG_BED_SHEETS, Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.PURPLE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.LIGHT_GRAY).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.BLACK).asItem());
        PatternTemplate.SEVEN_COLOR.offerTo(this, items, output, UItems.RAINBOW_BED_SHEETS, Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.RED).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.ORANGE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.YELLOW).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.BLUE).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.GREEN).asItem(), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.PURPLE).asItem());
    }

    public void offerCompactingRecipe(RecipeCategory category, ItemLike output, ItemLike input, int resultCount) {
        offerCompactingRecipe(category, output, input, getHasName(input), resultCount);
    }

    public void offerCompactingRecipe(RecipeCategory category, ItemLike output, ItemLike input, String criterionName, int resultCount) {
        ShapelessRecipeBuilder.shapeless(items, category, output, resultCount)
            .requires(input, 9).unlockedBy(criterionName, has(input))
            .save(this.output);
    }

    public void offerWaxingRecipes() {
        UBlockFamilies.WAXED_ZAP.getVariants().forEach((variant, output) -> {
            Block input = UBlockFamilies.ZAP.get(variant);
            offerWaxingRecipe(output, input);
        });
        offerWaxingRecipe(UBlocks.WAXED_ZAP_PLANKS, UBlocks.ZAP_PLANKS);
        offerWaxingRecipe(UBlocks.WAXED_ZAP_LOG, UBlocks.ZAP_LOG);
        offerWaxingRecipe(UBlocks.WAXED_ZAP_WOOD, UBlocks.ZAP_WOOD);
        offerWaxingRecipe(UBlocks.WAXED_STRIPPED_ZAP_LOG, UBlocks.STRIPPED_ZAP_LOG);
        offerWaxingRecipe(UBlocks.WAXED_STRIPPED_ZAP_WOOD, UBlocks.STRIPPED_ZAP_WOOD);
    }

    public void offerWaxingRecipe(ItemLike output, ItemLike input) {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.BUILDING_BLOCKS, output)
            .requires(Items.HONEYCOMB)
            .requires(input).unlockedBy(getHasName(input), has(input))
            .group(getItemName(output))
            .save(this.output, recipeId(getConversionRecipeName(output, Items.HONEYCOMB)));
    }

    public void offerCloudShapingRecipe(RecipeCategory category, ItemLike output, ItemLike input) {
        offerCloudShapingRecipe(category, output, input, 1);
    }

    public void offerCloudShapingRecipe(RecipeCategory category, ItemLike output, ItemLike input, int count) {
        createCloudShaping(Ingredient.of(input), category, output, count)
            .unlockedBy(getHasName(input), has(input))
            .save(this.output, recipeId(getConversionRecipeName(output, input) + "_cloud_shaping"));
    }

    public void offerGrowing(Block output, Block fuel, Block target) {
        GrowingRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output.defaultBlockState())
            .fuel(fuel.defaultBlockState())
            .target(target).criterion(getHasName(target), has(target))
            .offerTo(this.output);
    }

    public Criterion<?> conditionsFromMultipleItems(ItemLike... items) {
        return inventoryTrigger(
            Stream.of(items).map(item -> ItemPredicate.Builder.item().of(this.items, item).build()).toArray(ItemPredicate[]::new)
        );
    }

    public static ResourceKey<Recipe<?>> recipeId(String key) {
        return ResourceKey.create(Registries.RECIPE, Unicopia.id(key));
    }
}
