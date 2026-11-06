package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.world.entity.animal.equine.AbstractHorse;

public class SteedBehaviour<T extends AbstractHorse> extends EntityBehaviour<T> {

    @Override
    public void update(Pony player, T horse, Disguise spell) {
        boolean angry = !player.asEntity().onGround() && player.asEntity().isSprinting();
        boolean sneaking = isSneakingOnGround(player);

        angry |= sneaking;
        if (player.sneakingChanged() && sneaking) {
            horse.makeMad();
        }

        if (angry) {
            horse.setStanding(1);
        } else {
            horse.clearStanding();
        }
    }
}
