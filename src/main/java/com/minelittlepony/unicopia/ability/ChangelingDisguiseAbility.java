package com.minelittlepony.unicopia.ability;


import java.util.Optional;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.Trace;

/**
 * Changeling ability to disguise themselves as other players.
 */
public class ChangelingDisguiseAbility extends ChangelingFeedAbility {

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(player.asEntity().isCreative() || player.getMagicalReserves().getMana().getPercentFill() >= 0.9F);
    }

    @Override
    public boolean apply(Pony iplayer, Hit data) {
        Player player = iplayer.asEntity();

        if (prepare(iplayer).isEmpty()) {
            return false;
        }

        Trace trace = Trace.create(player, 10, 1, EquinePredicates.VALID_FOR_DISGUISE);

        Entity looked = trace.getEntity().map(AbstractDisguiseSpell::getAppearance).orElseGet(() -> trace.getBlockPos().map(pos -> {
            if (!iplayer.asWorld().isEmptyBlock(pos)) {
                return new FallingBlockEntity(player.level(), 0, 0, 0, iplayer.asWorld().getBlockState(pos));
            }
            return null;
        }).orElse(null));

        player.level().playSound(null, player.blockPosition(), USounds.ENTITY_PLAYER_CHANGELING_TRANSFORM, SoundSource.PLAYERS, 1.4F, 0.4F);

        Disguise currentDisguise = iplayer.getSpellSlot().get(SpellType.CHANGELING_DISGUISE)
            .orElseGet(() -> SpellType.CHANGELING_DISGUISE.withTraits().apply(iplayer, CastingMethod.INNATE));

        if (currentDisguise.isOf(looked)) {
            looked = null;
        }
        currentDisguise.setDisguise(looked);

        if (!player.isCreative()) {
            iplayer.getMagicalReserves().getMana().multiply(0.1F);
        }

        player.refreshDimensions();
        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getEnergy().add(2F);
        player.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getEnergy().set(0);
        player.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
    }
}
