package com.minelittlepony.unicopia.particle;

import com.minelittlepony.unicopia.util.shape.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Utility for spawning particles.
 */
public interface ParticleUtils {

    static PointGenerator getShapeFor(Entity entity) {
        final double halfDist = Math.abs(entity.getEyeHeight() / 1.5);
        final double middle = entity.getBoundingBox().minY + halfDist;
        return new Sphere(false, Math.abs((float)halfDist + entity.getBbWidth())).translate(new Vec3(entity.getX(), middle, entity.getZ()));
    }

    static void spawnParticles(ParticleOptions effect, Entity entity, int count) {
        spawnParticles(entity.level(), getShapeFor(entity), effect, count);
    }

    static void spawnParticles(ParticleOptions effect, Level world, Vec3 origin, int count) {
        spawnParticles(world, Sphere.UNIT_SPHERE.translate(origin), effect, count);
    }

    static void spawnParticles(Level world, PointGenerator points, ParticleOptions effect, int count) {
        points.randomPoints(count, world.getRandom()).forEach(point -> spawnParticle(world, effect, point, Vec3.ZERO));
    }

    static void spawnParticle(Level world, ParticleOptions effect, Vec3 pos, Vec3 vel) {
        spawnParticle(world, effect, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
    }

    static void spawnParticle(Level world, ParticleOptions effect, double x, double y, double z, double vX, double vY, double vZ) {
        if (world instanceof ServerLevel sw) {
            Vec3 vel = new Vec3(vX, vY, vZ);

            sw.sendParticles(effect, x, y, z, 1, vX, vY, vZ, vel.length());
        } else {
            world.addParticle(effect, x, y, z, vX, vY, vZ);
        }
    }
}
