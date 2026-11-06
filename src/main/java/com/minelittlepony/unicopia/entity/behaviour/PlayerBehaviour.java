package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PlayerBehaviour extends EntityBehaviour<Player> {
    @Override
    public void update(Living<?> source, Player entity, Disguise spell) {
        if (source.asEntity().getPose() != entity.getPose()) {
            entity.setPose(source.asEntity().getPose());
        }
        if (source.asEntity().isSwimming() != entity.isSwimming()) {
            entity.setSwimming(source.asEntity().isSwimming());
        }
        if (source.asEntity() instanceof LivingEntityDuck duck) {
            duck.copyLeaningAnglesFrom(((LivingEntityDuck)entity));
        }
    }

    @Override
    public boolean isEqual(Player a, Entity b) {
        return b instanceof Player p && p.getUUID().equals(b.getUUID());
    }
}
