package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.item.Items;

public class IronGolemBehaviour extends MobBehaviour<IronGolem> {
    @Override
    public void update(Pony player, IronGolem entity, Disguise spell) {
        super.update(player, entity, spell);
        boolean hasPoppy = player.asEntity().getItemInHand(InteractionHand.MAIN_HAND).is(Items.POPPY);
        if (hasPoppy != entity.getOfferFlowerTick() > 0) {
            entity.offerFlower(hasPoppy);
        }
    }
}
