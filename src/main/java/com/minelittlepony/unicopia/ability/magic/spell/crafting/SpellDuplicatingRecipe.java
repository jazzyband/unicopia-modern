package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.*;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.minelittlepony.unicopia.util.InventoryUtil;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A recipe for creating a new spell from input traits and items.
 */
public record SpellDuplicatingRecipe (IngredientWithSpell material) implements SpellbookRecipe {
    public static final MapCodec<SpellDuplicatingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IngredientWithSpell.CODEC.fieldOf("material").forGetter(recipe -> recipe.material)
    ).apply(instance, SpellDuplicatingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellDuplicatingRecipe> PACKET_CODEC = IngredientWithSpell.PACKET_CODEC.map(SpellDuplicatingRecipe::new, SpellDuplicatingRecipe::material);

    @Override
    public RecipeSerializer<SpellDuplicatingRecipe> getSerializer() {
        return URecipes.SPELL_DUPLICATING;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(SpellbookRecipeDisplay.of(builder -> {
            ItemStack[] spells = SpellType.REGISTRY.stream()
                    .filter(SpellType::isObtainable)
                    .map(i -> EnchantableItem.enchant(UItems.GEMSTONE.getDefaultInstance(), i))
                    .toArray(ItemStack[]::new);
            builder.input(UItems.BOTCHED_GEM.getDefaultInstance());
            builder.input(spells);
            builder.result(spells);
        }));
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean matches(Input inventory, Level world) {
        ItemStack stack = inventory.stackToModify();
        return InventoryUtil.stream(inventory)
                .limit(inventory.size() - 1)
                .filter(i -> !i.isEmpty())
                .noneMatch(i -> !i.is(UItems.GEMSTONE) || !EnchantableItem.isEnchanted(i))
                && material.test(stack)
                && !EnchantableItem.isEnchanted(stack);
    }

    @Override
    public ItemStack craft(Input inventory, Provider registries) {
        return InventoryUtil.stream(inventory)
            .filter(i -> i.is(UItems.GEMSTONE))
            .filter(EnchantableItem::isEnchanted)
            .map(stack -> stack.copy())
            .map(stack -> {
                stack.setCount(2);
                return stack;
            })
            .findFirst().get();
    }
}
