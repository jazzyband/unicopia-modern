package com.minelittlepony.unicopia.item;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.util.TypedActionResult;
import com.minelittlepony.unicopia.util.Untyped;

public interface EnchantableItem extends ItemLike {
    static CustomisedSpellType<?> getSpellEffect(ItemStack stack) {
        return EnchantableItem.getSpellKey(stack).withTraits(SpellTraits.of(stack));
    }

    static TypedActionResult<CustomisedSpellType<?>> consumeSpell(ItemStack stack, Player player, @Nullable Predicate<CustomisedSpellType<?>> filter, boolean consume) {
        SpellType<Spell> key = EnchantableItem.getSpellKey(stack);

        if (key.isEmpty()) {
            return TypedActionResult.pass(SpellType.EMPTY_KEY.withTraits());
        }

        CustomisedSpellType<?> result = key.withTraits(SpellTraits.of(stack));

        if (filter != null && !filter.test(result)) {
            return TypedActionResult.fail(SpellType.EMPTY_KEY.withTraits());
        }

        if (!player.level().isClientSide() && consume) {
            player.swing(player.getItemInHand(InteractionHand.OFF_HAND) == stack ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
                    net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
            player.getCooldowns().addCooldown(stack, 20);

            if (!player.isCreative()) {
                if (stack.getCount() == 1) {
                    unenchant(stack);
                } else {
                    player.addItem(unenchant(stack.split(1)));
                }
            }
        }

        return TypedActionResult.success(result);
    }

    static boolean isEnchanted(ItemStack stack) {
        return !getSpellKey(stack).isEmpty();
    }

    static ItemStack enchant(ItemStack stack, SpellType<?> type) {
        if (stack.isEmpty() || type.isEmpty()) {
            return unenchant(stack);
        }
        stack.set(UDataComponentTypes.STORED_SPELL, type);
        return type.getTraits().applyTo(stack);
    }

    static ItemStack unenchant(ItemStack stack) {
        stack.remove(UDataComponentTypes.STORED_SPELL);
        stack.remove(UDataComponentTypes.SPELL_TRAITS);
        return stack;
    }

    static Optional<SpellType<?>> getSpellKeyOrEmpty(ItemStack stack) {
        SpellType<?> type = getSpellKey(stack);
        return type.isEmpty() ? Optional.empty() : Optional.of(type);
    }

    static <T extends Spell> SpellType<T> getSpellKey(ItemStack stack) {
        return Untyped.cast(stack.getOrDefault(UDataComponentTypes.STORED_SPELL, SpellType.empty()));
    }
}
