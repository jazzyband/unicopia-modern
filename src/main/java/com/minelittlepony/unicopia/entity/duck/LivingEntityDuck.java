package com.minelittlepony.unicopia.entity.duck;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface LivingEntityDuck extends EntityDuck {
    void updateItemUsage(InteractionHand hand, ItemStack stack, int time);

    boolean isJumping();

    float getLeaningPitch();

    void setLeaningPitch(float pitch);

    float getLastLeaningPitch();

    void setLastLeaningPitch(float pitch);

    default void copyLeaningAnglesFrom(LivingEntityDuck other) {
        setLeaningPitch(other.getLeaningPitch());
        setLastLeaningPitch(other.getLastLeaningPitch());
    }

    static Vec3 serverPos(LivingEntity entity) {
        return entity.getPositionCodec().getBase();
    }
}
