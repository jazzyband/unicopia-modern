package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.rabbit.Rabbit;

public class HoppingBehaviour extends EntityBehaviour<LivingEntity> {
    @Override
    public void update(Pony player, LivingEntity entity, Disguise spell) {

        if (player.asEntity().onGround()) {
            if (player.asEntity().getDeltaMovement().horizontalDistanceSqr() > 0.01) {
                player.asEntity().jumpFromGround();
                startJump(entity);
            }
        } else if (player.landedChanged()) {
            startJump(entity);
        }
    }

    private void startJump(LivingEntity entity) {
        if (entity instanceof Rabbit rabbit) {
            rabbit.startJumping();
        }
    }
}
