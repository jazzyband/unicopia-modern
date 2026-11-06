package com.minelittlepony.unicopia.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;

public interface ParticleSpawner {
    ParticleSpawner EMPTY = (effect, pos, vel) -> {};

    void addParticle(ParticleOptions effect, Vec3 position, Vec3 velocity);
}
