package com.minelittlepony.unicopia.entity.player;

import net.minecraft.world.phys.Vec3;

/**
 * Interface for controlling flight.
 */
public interface Motion {
    /**
     * True if we're currently flying.
     */
    boolean isFlying();

    boolean isGliding();

    boolean isDiving();

    boolean isRainbooming();

    float getWingAngle();

    PlayerDimensions getDimensions();

    Vec3 getTrackedVelocity();
}
