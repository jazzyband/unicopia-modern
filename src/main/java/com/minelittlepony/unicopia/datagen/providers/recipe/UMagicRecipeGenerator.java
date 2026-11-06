package com.minelittlepony.unicopia.datagen.providers.recipe;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellDuplicatingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellEnhancingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.item.UItems;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public class UMagicRecipeGenerator extends RecipeProvider implements CraftingMaterialHelper {
    private final RecipeOutput output;

    UMagicRecipeGenerator(BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements, RecipeOutput exporter) {
        super(recipes, advancements);
        output = exporter;
    }

    @Override
    public void buildRecipes() {
        offerSpell(output, UItems.GEMSTONE, SpellType.DISPLACEMENT, SpellTraits.builder().with(Trait.KNOWLEDGE, 10).with(Trait.CHAOS, 10));
        offerSpell(output, UItems.GEMSTONE, SpellType.FROST, SpellTraits.builder().with(Trait.ICE, 10));
        offerSpell(output, UItems.GEMSTONE, SpellType.SCORCH, SpellTraits.builder().with(Trait.FIRE, 10));
        offerSpell(output, UItems.GEMSTONE, SpellType.SHIELD, SpellTraits.builder().with(Trait.STRENGTH, 10).with(Trait.FOCUS, 6).with(Trait.POWER, 10));
        offerSpell(output, UItems.GEMSTONE, SpellType.TRANSFORMATION, SpellTraits.builder().with(Trait.KNOWLEDGE, 10).with(Trait.LIFE, 10).with(Trait.CHAOS, 4));

        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.ARCANE_PROTECTION, SpellType.SHIELD, SpellTraits.builder().with(Trait.STRENGTH, 10).with(Trait.KNOWLEDGE, 18).with(Trait.DARKNESS, 1));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.BUBBLE, SpellType.CATAPULT, SpellTraits.builder().with(Trait.WATER, 9).with(Trait.AIR, 9));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.CATAPULT, SpellType.FLAME, SpellTraits.builder().with(Trait.FOCUS, 9).with(Trait.AIR, 9));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.CHILLING_BREATH, SpellType.FROST, SpellTraits.builder().with(Trait.ICE, 5).with(Trait.KNOWLEDGE, 10));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.DARK_VORTEX, SpellType.VORTEX, SpellTraits.builder().with(Trait.STRENGTH, 10).with(Trait.KNOWLEDGE, 8).with(Trait.DARKNESS, 9).with(Trait.CHAOS, 8));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.FEATHER_FALL, SpellType.SHIELD, SpellTraits.builder().with(Trait.KNOWLEDGE, 20).with(Trait.LIFE, 10).with(Trait.CHAOS, 4).with(Trait.GENEROSITY, 10));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.FIRE_BOLT, SpellType.FLAME, SpellTraits.builder().with(Trait.FOCUS, 9).with(Trait.FIRE, 30));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.FLAME, SpellType.SCORCH, SpellTraits.builder().with(Trait.FIRE, 15));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.INFERNAL, SpellType.FLAME, SpellTraits.builder().with(Trait.FIRE, 50).with(Trait.DARKNESS, 10));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.LIGHT, SpellType.FIRE_BOLT, SpellTraits.builder().with(Trait.ICE, 30).with(Trait.LIFE, 30).with(Trait.FOCUS, 10));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.MIMIC, SpellType.TRANSFORMATION, SpellTraits.builder().with(Trait.KNOWLEDGE, 19).with(Trait.LIFE, 10).with(Trait.CHAOS, 4));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.MIND_SWAP, SpellType.MIMIC, SpellTraits.builder().with(Trait.KNOWLEDGE, 19).with(Trait.LIFE, 10).with(Trait.CHAOS, 40));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.NECROMANCY, SpellType.SIPHONING, SpellTraits.builder().with(Trait.STRENGTH, 10).with(Trait.KNOWLEDGE, 8).with(Trait.DARKNESS, 19).with(Trait.CHAOS, 8).with(Trait.BLOOD, 10).with(Trait.POISON, 9));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.REVEALING, SpellType.SHIELD, SpellTraits.builder().with(Trait.KNOWLEDGE, 18).with(Trait.LIFE, 1).with(Trait.ORDER, 4));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.SIPHONING, SpellType.INFERNAL, SpellTraits.builder().with(Trait.BLOOD, 8).with(Trait.POISON, 10));
        offerSpellFromSpell(output, UItems.GEMSTONE, SpellType.VORTEX, SpellType.SHIELD, SpellTraits.builder().with(Trait.STRENGTH, 10).with(Trait.KNOWLEDGE, 8).with(Trait.AIR, 9));

        offerSpellFromTwoSpells(output, UItems.GEMSTONE, SpellType.DISPEL_EVIL, SpellType.ARCANE_PROTECTION, SpellType.DISPLACEMENT, SpellTraits.builder().with(Trait.KINDNESS, 1).with(Trait.POWER, 1));
        offerSpellFromTwoSpells(output, UItems.GEMSTONE, SpellType.HYDROPHOBIC, SpellType.FROST, SpellType.SHIELD, SpellTraits.builder().with(Trait.FOCUS, 6));
        offerSpellFromTwoSpells(output, UItems.GEMSTONE, SpellType.PORTAL, SpellType.DISPLACEMENT, SpellType.DARK_VORTEX, SpellTraits.builder().with(Trait.KNOWLEDGE, 18).with(Trait.CHAOS, 20));

        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.ALICORN_AMULET, SpellType.EMPTY_KEY)
            .base(UItems.GEMSTONE, SpellType.DARK_VORTEX)
            .traits(SpellTraits.builder().with(Trait.DARKNESS, 30).with(Trait.POWER, 30).with(Trait.BLOOD, 30))
            .offerTo(output);

        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.UNICORN_AMULET, SpellType.EMPTY_KEY)
            .base(UItems.BROKEN_ALICORN_AMULET, SpellType.EMPTY_KEY)
            .input(UItems.PEGASUS_AMULET, SpellType.EMPTY_KEY)
            .input(UItems.CRYSTAL_HEART, SpellType.EMPTY_KEY)
            .input(UItems.GROGARS_BELL, SpellType.EMPTY_KEY)
            .input(Items.TOTEM_OF_UNDYING, SpellType.EMPTY_KEY)
            .traits(SpellTraits.builder())
            .criterion(getHasName(UItems.BROKEN_ALICORN_AMULET), has(UItems.BROKEN_ALICORN_AMULET))
            .offerTo(output, recipeId("unicorn_amulet"));

        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.DRAGON_BREATH_SCROLL, SpellType.EMPTY_KEY)
            .base(Items.PAPER, SpellType.EMPTY_KEY)
            .input(Items.PAPER, SpellType.EMPTY_KEY)
            .traits(SpellTraits.builder().with(Trait.FIRE, 1))
            .offerTo(output, recipeId("dragon_breath_scroll"));

        ComplexSpellcraftingRecipeJsonBuilder.create(SpellDuplicatingRecipe::new, UItems.BOTCHED_GEM).offerTo(output, recipeId("spell_duplicating"));
        ComplexSpellcraftingRecipeJsonBuilder.create(SpellEnhancingRecipe::new, UItems.BOTCHED_GEM).offerTo(output, recipeId("trait_combining_botched_gem"));
        ComplexSpellcraftingRecipeJsonBuilder.create(SpellEnhancingRecipe::new, UItems.GEMSTONE).offerTo(output, recipeId("trait_combining_gemstone"));

        AltarRecipeJsonBuilder.create(RecipeCategory.TOOLS, UItems.SPECTRAL_CLOCK)
            .input(Items.CLOCK).unlockedBy("has_clock", has(Items.CLOCK))
            .save(output);
        AltarRecipeJsonBuilder.create(RecipeCategory.TOOLS, UItems.TOTEM_OF_DYING)
            .input(Items.TOTEM_OF_UNDYING).unlockedBy("has_totem", has(Items.TOTEM_OF_UNDYING))
            .save(output);
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
