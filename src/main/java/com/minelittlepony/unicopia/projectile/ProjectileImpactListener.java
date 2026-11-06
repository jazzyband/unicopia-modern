package com.minelittlepony.unicopia.projectile;

import net.minecraft.world.entity.projectile.Projectile;

public interface ProjectileImpactListener {
    boolean onProjectileImpact(Projectile projectile);
}
