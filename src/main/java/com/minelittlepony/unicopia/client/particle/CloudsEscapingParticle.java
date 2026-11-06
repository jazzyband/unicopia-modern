package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.shape.Sphere;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

public class CloudsEscapingParticle extends GroundPoundParticle {

    public CloudsEscapingParticle(SimpleParticleType effect, ClientLevel world, double x, double y, double z, double dX, double dY, double dZ) {
        super(effect, world, x, y, z, dX, dY, dZ);
        lifetime = 200;
        hasPhysics = false;
    }

    @Override
    public void spawnChildParticles() {
        this.yd += 0.125;

        Vec3 center = getPos();

        double variance = age / lifetime;
        Vec3 vel = new Vec3(
                (level.getRandom().nextFloat() - 0.5) * (0.125 + variance),
                yd * 0.2,
                (level.getRandom().nextFloat() - 0.5) * (0.125 + variance)
        );

        double columnHeight = 1 + age / 30;
        new Sphere(true, columnHeight)
            .translate(center)
            .randomPoints(random)
            .forEach(point -> {
                ParticleUtils.spawnParticle(level, ParticleTypes.CLOUD, point, vel);
        });
    }
}
