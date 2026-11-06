package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.item.*;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.minelittlepony.unicopia.recipe.URecipes;

/**
 * Recipe for adding traits to an existing spell.
 */
public record SpellEnhancingRecipe (IngredientWithSpell material) implements SpellbookRecipe {
    public static final MapCodec<SpellEnhancingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IngredientWithSpell.CODEC.fieldOf("material").forGetter(recipe -> recipe.material)
    ).apply(instance, SpellEnhancingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellEnhancingRecipe> PACKET_CODEC = IngredientWithSpell.PACKET_CODEC.map(SpellEnhancingRecipe::new, SpellEnhancingRecipe::material);

    @Override
    public RecipeSerializer<SpellEnhancingRecipe> getSerializer() {
        return URecipes.TRAIT_COMBINING;
    }

    public IngredientWithSpell getBaseMaterial() {
        return material;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(SpellbookRecipeDisplay.of(builder -> {
            builder.input(material.getMatchingStacks());
        }));
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public boolean matches(Input inventory, Level world) {
        ItemStack stack = inventory.stackToModify();
        return material.test(stack) && EnchantableItem.isEnchanted(stack);
    }

    @Override
    public ItemStack craft(Input inventory, Provider registries) {
        return SpellTraits.of(inventory.stackToModify())
                .add(inventory.traits())
                .applyTo(inventory.stackToModify());
    }
}
