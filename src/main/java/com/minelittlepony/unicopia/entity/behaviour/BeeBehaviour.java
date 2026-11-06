package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.entity.Living;
import net.minecraft.world.entity.animal.bee.Bee;

public class BeeBehaviour extends EntityBehaviour<Bee> {
    @Override
    public Bee onCreate(Bee entity, EntityAppearance context, boolean replaceOld) {
        super.onCreate(entity, context, replaceOld);
        if (replaceOld && entity.level().isClientSide()) {
            InteractionManager.getInstance().playLoopingSound(entity, InteractionManager.SOUND_BEE, entity.getId());
        }
        return entity;
    }

    @Override
    public void update(Living<?> source, Bee entity, Disguise spell) {
        if (source.asEntity().isShiftKeyDown()) {
            entity.setPersistentAngerEndTime(entity.level().getGameTime() + 10);
        } else {
            entity.setPersistentAngerEndTime(0);
        }
    }
}
