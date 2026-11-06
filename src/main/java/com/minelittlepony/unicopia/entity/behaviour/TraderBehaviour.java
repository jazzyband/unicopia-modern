package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.world.entity.npc.villager.AbstractVillager;

public class TraderBehaviour extends EntityBehaviour<AbstractVillager> {
    @Override
    public void update(Pony pony, AbstractVillager entity, Disguise spell) {
        if (pony.sneakingChanged() && pony.asEntity().isShiftKeyDown()) {
            entity.setUnhappyCounter(40);

            if (!entity.level().isClientSide()) {
               entity.playSound(USounds.Vanilla.VILLAGER_NO, 1, 1);
            }
        }
    }
}
