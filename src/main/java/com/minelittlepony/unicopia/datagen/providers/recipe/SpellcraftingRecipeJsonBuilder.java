package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellCraftingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.TraitIngredient;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.advancement.TraitDiscoveredCriterion;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;
import com.minelittlepony.unicopia.datagen.providers.SpellbookChapterProvider.MagicRecipeExporter;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.UItems;

public class SpellcraftingRecipeJsonBuilder {
    private final Map<String, Criterion<?>> criterions = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final RecipeCategory category;

    private IngredientWithSpell base = IngredientWithSpell.mundane(UItems.GEMSTONE);
    private final SpellType<?> spell;
    private final ItemLike gem;
    private final List<IngredientWithSpell> ingredients = new ArrayList<>();
    private SpellTraits traits = SpellTraits.EMPTY;

    private final ChapterPageElement.Ingredients.Builder chapterIngredients = ChapterPageElement.Ingredients.builder();

    public static SpellcraftingRecipeJsonBuilder create(RecipeCategory category, ItemLike gem, SpellType<?> spell) {
        return new SpellcraftingRecipeJsonBuilder(category, gem, spell);
    }

    private SpellcraftingRecipeJsonBuilder(RecipeCategory category, ItemLike gem, SpellType<?> spell) {
        this.category = category;
        this.gem = gem;
        this.spell = spell;
    }

    public SpellcraftingRecipeJsonBuilder base(ItemLike gem, SpellType<?> spell) {
        base = IngredientWithSpell.of(gem, spell);
        return this;
    }

    public SpellcraftingRecipeJsonBuilder input(ItemLike gem, SpellType<?> spell) {
        ingredients.add(IngredientWithSpell.of(gem, spell));
        chapterIngredients.spell(1, spell);
        return this;
    }

    public SpellcraftingRecipeJsonBuilder traits(SpellTraits.Builder traits) {
        this.traits = traits.build();
        this.traits.forEach(trait -> {
            chapterIngredients.trait((int)Math.ceil(trait.getValue()), trait.getKey());
        });
        return this;
    }

    public SpellcraftingRecipeJsonBuilder criterion(String name, Criterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    public SpellcraftingRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> key) {
        if (!traits.isEmpty()) {
            criterions.put("has_traits", TraitDiscoveredCriterion.create(traits.keys()));
        }
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + key.identifier());
        Advancement.Builder advancementBuilder = exporter.advancement()
            .rewards(AdvancementRewards.Builder.recipe(key))
            .requirements(AdvancementRequirements.Strategy.OR);
        criterions.forEach(advancementBuilder::addCriterion);
        exporter.accept(key, new SpellCraftingRecipe(base,
                TraitIngredient.of(traits), ingredients, EnchantableItem.enchant(gem.asItem().getDefaultInstance(), spell)),
                advancementBuilder.build(key.identifier().withPrefix("recipes/" + category.getFolderName() + "/")));
        if (exporter instanceof MagicRecipeExporter magicExporter) {
            magicExporter.acceptChapterIngredients(key, chapterIngredients.build());
        }
    }

    public void offerTo(RecipeOutput exporter) {
        offerTo(exporter, ResourceKey.create(Registries.RECIPE, spell.getId()));
    }

    public void offerTo(RecipeOutput exporter, String recipePath) {
        Identifier recipeId = Identifier.parse(recipePath);
        if (recipeId.equals(spell.getId())) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, ResourceKey.create(Registries.RECIPE, recipeId));
    }
}
