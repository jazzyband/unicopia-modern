package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A recipe for creating a new spell from input traits and items.
 */
public class SpellCraftingRecipe implements SpellbookRecipe {
    private static final Codec<ItemStackTemplate> RESULT_CODEC = ItemStackTemplate.CODEC;

    public static final MapCodec<SpellCraftingRecipe> CODEC = RecordCodecBuilder.<SpellCraftingRecipe>mapCodec(instance -> instance.group(
            IngredientWithSpell.CODEC.fieldOf("material").forGetter(recipe -> recipe.material),
            TraitIngredient.CODEC.fieldOf("traits").forGetter(recipe -> recipe.requiredTraits),
            IngredientWithSpell.CODEC.listOf().fieldOf("ingredients").forGetter(recipe -> recipe.requiredItems),
            RESULT_CODEC.fieldOf("result").forGetter(recipe -> recipe.output)
    ).apply(instance, SpellCraftingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellCraftingRecipe> PACKET_CODEC = StreamCodec.composite(
            IngredientWithSpell.PACKET_CODEC, recipe -> recipe.material,
            TraitIngredient.PACKET_CODEC, recipe -> recipe.requiredTraits,
            IngredientWithSpell.PACKET_CODEC.apply(ByteBufCodecs.list()), recipe -> recipe.requiredItems,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.output,
            SpellCraftingRecipe::new
    );

    /**
     * The ingredient to modify
     */
    final IngredientWithSpell material;

    /**
     * The required traits
     */
    final TraitIngredient requiredTraits;

    /**
     * Items required for crafting.
     */
    final List<IngredientWithSpell> requiredItems;

    /**
     * The resulting item
     */
    final ItemStackTemplate output;

    public SpellCraftingRecipe(IngredientWithSpell material, TraitIngredient requiredTraits, List<IngredientWithSpell> requiredItems, ItemStackTemplate output) {
        this.material = material;
        this.requiredTraits = requiredTraits;
        this.requiredItems = requiredItems;
        this.output = output;
    }

    public SpellCraftingRecipe(IngredientWithSpell material, TraitIngredient requiredTraits, List<IngredientWithSpell> requiredItems, ItemStack output) {
        this(material, requiredTraits, requiredItems, ItemStackTemplate.fromNonEmptyStack(output));
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(SpellbookRecipeDisplay.of(builder -> {
            builder.input(material.getMatchingStacks());
            for (var ingredient : requiredItems) {
                builder.input(ingredient.getMatchingStacks());
            }
            requiredTraits.min().ifPresent(min -> {
                min.forEach(e -> builder.input(e.getKey(), e.getValue()));
            });
            builder.result(output.create());
        }));
    }

    @Override
    public int getPriority() {
        return requiredItems.isEmpty() ? 0 : -1;
    }

    @Override
    public boolean matches(Input inventory, Level world) {

        if (!material.test(inventory.stackToModify()) || !requiredTraits.test(inventory.traits())) {
            return false;
        }

        if (requiredItems.isEmpty()) {
            return true;
        }

        var outstandingRequirements = new ArrayList<>(requiredItems);
        var ingredients = new ArrayList<>(inventory.stacks());

        outstandingRequirements.removeIf(requirement -> {
            var found = ingredients.stream().filter(pair -> requirement.test(pair.getSecond())).findAny();
            found.ifPresent(ingredients::remove);
            return found.isPresent();
        });

        return outstandingRequirements.isEmpty();
    }

    @Override
    public ItemStack craft(Input inventory, Provider registries) {
        return output.create();
    }

    @Override
    public RecipeSerializer<SpellCraftingRecipe> getSerializer() {
        return URecipes.TRAIT_REQUIREMENT;
    }
}
