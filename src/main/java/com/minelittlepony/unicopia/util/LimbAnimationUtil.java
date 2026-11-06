package com.minelittlepony.unicopia.util;

import net.minecraft.world.entity.WalkAnimationState;

public interface LimbAnimationUtil {

    static void resetToZero(WalkAnimationState animator) {
        animator.stop();
    }

    static void copy(WalkAnimationState from, WalkAnimationState to) {
        float lastSpeed = from.speed(0);
        float speed = from.speed();
        float animationProgress = from.position(); // animationProgress * timeScale

        resetToZero(to);
        to.setSpeed(lastSpeed);                  // lastSpeed into speed
                                                 // speed -> lastSpeed
        to.update(animationProgress, 1, 1); // animationProgress into animationProgress, timeScale -> 1
        to.setSpeed(speed);                      // speed into speed
    }
}
