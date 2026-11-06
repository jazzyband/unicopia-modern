package com.minelittlepony.unicopia.item;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.PlayerCharmTracker;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.group.MultiItem;
import com.minelittlepony.unicopia.util.TypedActionResult;
import com.mojang.serialization.Codec;

public class GemstoneItem extends Item implements MultiItem {

    public GemstoneItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        InteractionResult result = super.use(world, user, hand);

        if (!result.consumesAction()) {
            ItemStack stack = user.getItemInHand(hand);
            PlayerCharmTracker charms = Pony.of(user).getCharms();

            if (!Pony.of(user).getCompositeRace().canCast()) {
                return result;
            }

            hand = user.isShiftKeyDown() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

            TypedActionResult<CustomisedSpellType<?>> spell = EnchantableItem.consumeSpell(stack, user, ((Predicate<CustomisedSpellType<?>>)charms.getEquippedSpell(hand)::equals).negate(), true);

            CustomisedSpellType<?> existing = charms.getEquippedSpell(hand);

            if (!existing.isEmpty()) {

                if (stack.getCount() == 1) {
                    stack = existing.traits().applyTo(EnchantableItem.enchant(stack, existing.type()));
                } else {
                    user.addItem(existing.traits().applyTo(EnchantableItem.enchant(stack.split(1), existing.type())));
                }
            }

            if (spell.result().consumesAction()) {
                charms.equipSpell(hand, spell.value());
            } else {

                if (existing.isEmpty()) {
                    return result;
                }

                charms.equipSpell(hand, SpellType.EMPTY_KEY.withTraits());
            }

            user.getCooldowns().addCooldown(stack, 20);
            return InteractionResult.SUCCESS;
        }

        return result;
    }

    @Override
    public List<ItemStack> getDefaultStacks() {
        return SpellType.REGISTRY.stream()
                .filter(SpellType::isObtainable)
                .sorted(
                        Comparator.<SpellType<?>, GemstoneItem.Shape>comparing(SpellType::getGemShape).thenComparing(Comparator.comparing(SpellType::getAffinity))
                )
                .map(type -> EnchantableItem.enchant(getDefaultInstance(), type))
                .toList();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || (InteractionManager.getInstance().getClientSpecies().canCast() && EnchantableItem.isEnchanted(stack));
    }

    @Override
    public Component getName(ItemStack stack) {
        if (EnchantableItem.isEnchanted(stack)) {
            if (!InteractionManager.getInstance().getClientSpecies().canCast()) {
                return Component.translatable(getDescriptionId() + ".obfuscated");
            }

            return Component.translatable(getDescriptionId() + ".enchanted", EnchantableItem.getSpellKey(stack).getName());
        }
        return super.getName(stack);
    }

    public enum Shape implements StringRepresentable {
        ARROW,
        BRUSH,
        CROSS,
        DONUT,
        FLAME,
        ICE,
        LAMBDA,
        RING,
        ROCKET,
        ROUND,
        SHIELD,
        SKULL,
        SPLINT,
        STAR,
        TRIANGLE,
        VORTEX,
        WAVE;

        public static final int LENGTH = values().length;
        public static final Codec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);

        private final String name = name().toLowerCase(Locale.ROOT);

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
