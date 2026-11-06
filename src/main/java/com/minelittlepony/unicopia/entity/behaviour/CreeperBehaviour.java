package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;

public class CreeperBehaviour extends EntityBehaviour<Creeper> {
    @Override
    public void update(Living<?> source, Creeper entity, Disguise spell) {
        int fuseCountDown = spell.getAppearance().getOrCreateTag().getIntOr("fuseCountdown", 0);

        boolean trigger = isSneakingOnGround(source);

        if (trigger) {
            fuseCountDown++;
        } else if (fuseCountDown > 0) {
            fuseCountDown--;
        }

        int max = source.isClient() ? 90 : 30;

        fuseCountDown = Mth.clamp(fuseCountDown, 0, max + 1);

        if (fuseCountDown <= max && trigger) {
            entity.setSwellDir(1);
        } else {
            entity.setSwellDir(-1);
            entity.setTarget(null);
            entity.getSensing().tick();
        }

        spell.getAppearance().getOrCreateTag().putInt("fuseCountdown", fuseCountDown);
    }
}
