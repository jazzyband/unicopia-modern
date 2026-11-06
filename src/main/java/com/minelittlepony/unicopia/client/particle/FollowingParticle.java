package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;

public class FollowingParticle extends NoRenderParticle {

    private final FollowingParticleEffect parameters;

    private final Particle particle;

    private float scale;

    public FollowingParticle(FollowingParticleEffect parameters, SpriteSet provider, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.scale = 0.1f * (this.random.nextFloat() * 0.5f + 0.5f) * 2.0f;
        setLifetime(3);
        scale(0.125F);
        this.parameters = parameters;
        this.hasPhysics = false;
        this.particle = parameters.childEffect().map(child -> Minecraft.getInstance().particleEngine.createParticle(child, x, y, z, velocityX, velocityY, velocityZ)).orElse(null);
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
        if (this.particle == null || !this.particle.isAlive()) {
            remove();
        }

        super.tick();

        Vec3 target = parameters.target().getPosition(level);
        Vec3 pos = new Vec3(x, y, z);

        if (scale * 1.5F < 0.5F) {
            scale(1.5F);
        }

        double distance = pos.distanceTo(target);
        if (distance > 1) {
            age = 0;
        }

        Vec3 motion = target.subtract(pos).normalize().scale(Math.min(distance, parameters.followSpeed()));
        move(motion.x, motion.y, motion.z);
    }

    @Override
    public String toString() {
        return super.toString() + ", Speed " + parameters.followSpeed() + ", Target (" + parameters.target() + ") Sub-Particle (" + particle + ")";
    }
}
