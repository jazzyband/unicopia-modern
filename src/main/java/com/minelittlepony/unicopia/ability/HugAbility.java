package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.mob.FriendlyCreeperEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;

/**
 * Ability to hug mobs. Not all of them are receptive to your advances though, so be careful!
 */
public class HugAbility extends CarryAbility {
    @Override
    public boolean apply(Pony pony, Hit data) {
        Player player = pony.asEntity();
        LivingEntity rider = findRider(player, pony.asWorld());

        dropAllPassengers(player);

        pony.setAnimation(Animation.ARMS_FORWARD, Animation.Recipient.ANYONE);

        if (rider instanceof Creeper creeper) {
            FriendlyCreeperEntity friendlyCreeper = creeper.convertTo(UEntities.FRIENDLY_CREEPER, ConversionParams.single(creeper, true, true), e -> {
                e.startRiding(player, true, true);
                Living.getOrEmpty(e).ifPresent(living -> living.setCarrier(player));
            });
            if (friendlyCreeper != null) {
                player.level().addFreshEntity(friendlyCreeper);
            }
        } else if (rider instanceof FriendlyCreeperEntity creeper) {
            creeper.startRiding(player, true, true);
            Living.getOrEmpty(creeper).ifPresent(living -> living.setCarrier(player));
        } else if (rider != null) {
            rider.randomTeleport(player.getX(), player.getY() + 0.5, player.getZ(), false, state -> true);
            rider.setYRot(player.getYRot() + 180);

            if (rider instanceof FriendlyCreeperEntity) {
                pony.spawnParticles(ParticleTypes.HEART, 10);
            }
        }

        Living.transmitPassengers(player);
        return true;
    }
}
