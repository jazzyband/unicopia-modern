package com.minelittlepony.unicopia.item;

import net.minecraft.world.damagesource.DamageSource;

public interface DamageChecker {
    boolean takesDamageFrom(DamageSource source);
}
