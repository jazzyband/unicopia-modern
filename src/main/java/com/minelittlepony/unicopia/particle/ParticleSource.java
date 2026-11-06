package com.minelittlepony.unicopia.particle;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.EntityConvertable;
import com.minelittlepony.unicopia.util.shape.PointGenerator;

public interface ParticleSource<E extends Entity> extends ParticleSpawner, EntityConvertable<E> {

    default void spawnParticles(ParticleOptions particleId, Supplier<Vec3> posSupplier, Supplier<Vec3> velSupplier, int count) {
        for (int i = 0; i < count; i++) {
            spawnParticle(particleId, posSupplier, velSupplier);
        }
    }

    default void spawnParticle(ParticleOptions particleId, Supplier<Vec3> posSupplier, Supplier<Vec3> velSupplier) {
        addParticle(particleId, getOriginVector().add(posSupplier.get()), velSupplier.get());
    }

    default void spawnParticles(ParticleOptions particleId, int count) {
        ParticleUtils.spawnParticles(particleId, asEntity(), count);
    }

    default void spawnParticles(PointGenerator area, int count, Consumer<Vec3> particleSpawner) {
        spawnParticles(getOriginVector(), area, count, particleSpawner);
    }

    default void spawnParticles(Vec3 pos, PointGenerator area, int count, Consumer<Vec3> particleSpawner) {
        area.translate(pos).randomPoints(count, asWorld().getRandom()).forEach(particleSpawner);
    }

    @Override
    default void addParticle(ParticleOptions effect, Vec3 position, Vec3 velocity) {
        ParticleUtils.spawnParticle(asWorld(), effect, position, velocity);
    }
}
