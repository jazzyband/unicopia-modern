package com.minelittlepony.unicopia.client.particle;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import com.minelittlepony.unicopia.particle.ParticleSpawner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A connection class for updating and persisting an attached particle effect.
 */
public class ClientBoundParticleSpawner implements ParticleSpawner {
    private static final Map<UUID, Entry> SPAWNED_PARTICLES = new HashMap<>();

    private final UUID id;
    private WeakReference<BooleanSupplier> attachment = new WeakReference<>(null);

    private final Minecraft client = Minecraft.getInstance();

    public ClientBoundParticleSpawner(UUID id) {
        this.id = id;
    }

    @Override
    public void addParticle(ParticleOptions effect, Vec3 pos, Vec3 vel) {
        BooleanSupplier a = attachment.get();
        if ((a == null || !a.getAsBoolean())) {
            SPAWNED_PARTICLES.values().removeIf(set -> !set.getAsBoolean());
            attachment = new WeakReference<>(SPAWNED_PARTICLES.computeIfAbsent(id, i -> {
                return new Entry(
                        new WeakReference<>(client.level),
                        new WeakReference<>(client.particleEngine.createParticle(effect, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z))
                );
            }));
        }
    }

    private record Entry (WeakReference<Level> world, WeakReference<Particle> particle) implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            if (world.get() == null || world.get() != Minecraft.getInstance().level) {
                return false;
            }

            Particle particle = this.particle.get();
            return particle != null && particle.isAlive();
        }
    }
}
