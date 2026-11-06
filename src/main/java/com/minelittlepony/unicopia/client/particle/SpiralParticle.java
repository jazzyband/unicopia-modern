package com.minelittlepony.unicopia.client.particle;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.particle.SpiralParticleEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SpiralParticle extends NoRenderParticle {

    private final SpiralParticleEffect parameters;

    @Nullable
    private final Particle particle;

    private float scale;

    public SpiralParticle(SpiralParticleEffect parameters, SpriteSet provider, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.scale = 0.1f * (random.nextFloat() * 0.5f + 0.5f) * 2.0f;
        setLifetime(3);
        scale(0.125F);
        this.parameters = parameters;
        this.hasPhysics = false;
        this.particle = Minecraft.getInstance().particleEngine.createParticle(parameters.effect(), x, y, z, velocityX, velocityY, velocityZ);
        this.particle.setLifetime(1000);
    }

    @Override
    public Particle scale(float scale) {
        this.scale *= scale;
        super.scale(scale);
        return this;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        super.move(dx, dy, dz);
        if (particle != null) {
            particle.setPos(x, y, z);
        }
    }

    @Override
    public void tick() {
        if (particle == null || !particle.isAlive()) {
            remove();
        }

        super.tick();

        Vec3 target = parameters.centerPoint().getPosition(level);
        Vec3 pos = new Vec3(x, y, z);

        if (scale * 1.5F < 0.5F) {
            scale(1.5F);
        }

        double distance = pos.distanceTo(target);
        if (distance > 0) {
            age = 0;
        }

        Vec3 radial = target.subtract(pos).normalize();
        Vec3 tangent = radial.yRot(Mth.HALF_PI).scale(parameters.angularVelocity() * 0.9F);
        Vec3 motion = radial.scale(parameters.angularVelocity() * 0.1F).add(tangent);
        move(motion.x, motion.y, motion.z);
    }

    @Override
    public String toString() {
        return super.toString() + ", Angular Velocity " + parameters.angularVelocity() + ", Target (" + parameters.centerPoint() + ") Sub-Particle (" + particle + ")";
    }
}
