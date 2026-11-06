package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.TypedActionResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;

abstract class AbstractSpellCastingAbility implements Ability<Hit> {
    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Override
    public Component getName(Pony player) {
        CustomisedSpellType<?> spell = player.getCharms().getEquippedSpell(player.getCharms().getHand());
        TypedActionResult<CustomisedSpellType<?>> gemSpell = player.getCharms().getSpellInHand(false);
        var active = !player.getAbilities().getStat(AbilitySlot.PRIMARY).getActiveAbility().isEmpty();
        if (!spell.isEmpty()) {
            if (active) {
                if (gemSpell.result().consumesAction()) {
                    return Component.translatable(getTranslationKey() + ".with_spell.hand",
                        gemSpell.value().type().getName().copy().withStyle(gemSpell.value().type().getAffinity().getColor())
                    );
                }

                return Component.translatable(getTranslationKey() + ".with_spell.active",
                        spell.type().getName().copy().withStyle(spell.type().getAffinity().getColor())
                );
            }

            return Component.translatable(getTranslationKey() + ".with_spell" + (gemSpell.result().consumesAction() ? ".replacing" : ""),
                spell.type().getName().copy().withStyle(spell.type().getAffinity().getColor()),
                gemSpell.value().type().getName().copy().withStyle(gemSpell.value().type().getAffinity().getColor())
            );
        }
        return Ability.super.getName(player);
    }

    @Override
    public int getColor(Pony player) {
        TypedActionResult<CustomisedSpellType<?>> newSpell = player.getCharms().getSpellInHand(false);

        if (newSpell.result() != InteractionResult.FAIL) {
            return newSpell.value().type().getColor();
        }
        return -1;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Hit> getSerializer() {
        return Hit.CODEC;
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
