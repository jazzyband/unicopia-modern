package com.minelittlepony.unicopia.entity.player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.util.Copyable;
import com.minelittlepony.unicopia.util.TypedActionResult;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.mojang.serialization.Codec;

public class PlayerCharmTracker implements NbtSerialisable, Copyable<PlayerCharmTracker> {
    private static final Codec<List<CustomisedSpellType<?>>> HAND_SPELLS_CODEC = CustomisedSpellType.CODEC.listOf();

    private final Pony pony;

    private CustomisedSpellType<?>[] handSpells = new CustomisedSpellType<?>[] {
        SpellType.SHIELD.withTraits(),
        SpellType.CATAPULT.withTraits()
    };

    PlayerCharmTracker(Pony pony) {
        this.pony = pony;
    }

    public CustomisedSpellType<?>[] getHandSpells() {
        return handSpells;
    }

    public CustomisedSpellType<?> getEquippedSpell(InteractionHand hand) {
        return handSpells[hand.ordinal()] == null ? SpellType.EMPTY_KEY.withTraits() : handSpells[hand.ordinal()];
    }

    public InteractionHand getHand() {
        return pony.asEntity().isShiftKeyDown() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    public TypedActionResult<CustomisedSpellType<?>> getSpellInHand(boolean consume) {
        return getSpellInHand(getHand(), consume);
    }

    public TypedActionResult<CustomisedSpellType<?>> getSpellInHand(InteractionHand hand, boolean consume) {
        return Stream.of(pony.asEntity().getMainHandItem(), pony.asEntity().getOffhandItem())
                .filter(EnchantableItem::isEnchanted)
                .map(stack -> EnchantableItem.consumeSpell(stack, pony.asEntity(), null, consume))
                .findFirst()
                .orElse(getEquippedSpell(hand).toAction());
    }

    public CustomisedSpellType<?> equipSpell(InteractionHand hand, CustomisedSpellType<?> spell) {
        CustomisedSpellType<?> previous = handSpells[hand.ordinal()];
        handSpells[hand.ordinal()] = spell;
        if (!Objects.equals(previous, spell)) {
            pony.asEntity().playSound(USounds.GUI_SPELL_EQUIP.value(), 0.25F, 1.75F);
        }
        pony.sendUpdatePacket();
        return previous;
    }

    @Override
    public void copyFrom(PlayerCharmTracker old, boolean alive) {
        for (int i = 0; i < handSpells.length; i++) {
            handSpells[i] = old.handSpells[i];
        }
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        compound.store("handSpells", HAND_SPELLS_CODEC, Arrays.asList(handSpells));
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        compound.read("handSpells", HAND_SPELLS_CODEC).ifPresent(list -> {
            for (int i = 0; i < handSpells.length && i < list.size(); i++) {
                handSpells[i] = list.get(i);
            }
        });
    }
}
