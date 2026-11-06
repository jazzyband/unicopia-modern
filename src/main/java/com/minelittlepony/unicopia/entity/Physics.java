package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface Physics extends NbtSerialisable {
    float DEFAULT_GRAVITY_FACTOR = 1;

    double calcGravity(double worldConstant);

    Vec3 getMotionAngle();

    float getGravityModifier();

    float getPersistantGravityModifier();

    float getBaseGravityModifier();

    void setBaseGravityModifier(float constant);

    boolean isFlying();

    BlockPos getHeadPosition();

    default boolean isGravityNegative() {
        return getGravityModifier() < 0;
    }

    default int getGravitySignum() {
        return (int)Math.signum(getGravityModifier());
    }

    default AABB getBoxAtPosition(Vec3 pos) {
        return getBoxAtPosition(pos, isGravityNegative());
    }

    AABB getBoxAtPosition(Vec3 pos, boolean inverted);
}
