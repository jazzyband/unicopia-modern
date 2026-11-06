package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.entity.Living;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public class MinecartBehaviour extends EntityBehaviour<AbstractMinecart> {

    @Override
    public AbstractMinecart onCreate(AbstractMinecart entity, EntityAppearance context, boolean replaceOld) {
        super.onCreate(entity, context, replaceOld);
        if (replaceOld && entity.level().isClientSide()) {
            InteractionManager.getInstance().playLoopingSound(entity, InteractionManager.SOUND_MINECART, entity.getId());
        }
        return entity;
    }

    @Override
    public void update(Living<?> source, AbstractMinecart entity, Disguise spell) {
        entity.setYRot(entity.getYRot() - 90);
        entity.yRotO -= 90;

        entity.setXRot(0);
        entity.xRotO = 0;

        LivingEntity living = source.asEntity();
        if (living.hurtTime > 0) {
            entity.setHurtTime(living.hurtTime);
            entity.setDamage(1);
            entity.setHurtDir(20 + (int)source.asEntity().fallDistance / 10);
        }
    }
}
