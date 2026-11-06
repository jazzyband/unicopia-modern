package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.world.entity.animal.camel.Camel;

public class CamelBehaviour extends EntityBehaviour<Camel> {
    @Override
    public void update(Pony player, Camel entity, Disguise spell) {
        entity.setDashing(player.asEntity().isSprinting());

        if (player.asEntity().isShiftKeyDown()) {
            entity.sitDown();
        } else {
            entity.standUp();
        }
    }
}
