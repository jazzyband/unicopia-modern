package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;

public class HealthDrainParticle extends SingleQuadParticle {

    private final FollowingParticleEffect effect;

    public static Particle create(FollowingParticleEffect effect, SpriteSet provider, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if (effect.childEffect().isPresent()) {
            return new FollowingParticle(effect, provider, world, x, y, z, velocityX, velocityY, velocityZ);
        }
        return new HealthDrainParticle(effect, provider, world, x, y, z, velocityX, velocityY, velocityZ);
    }

    protected HealthDrainParticle(FollowingParticleEffect effect, SpriteSet provider, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, provider.get(world.getRandom()));
        setLifetime(3);
        scale(0.125F);
        this.effect = effect;
        this.hasPhysics = false;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 target = effect.target().getPosition(level);
        Vec3 pos = new Vec3(x, y, z);

        if (this.quadSize * 1.5F < 0.5F) {
            scale(1.5F);
        }

        double distance = pos.distanceTo(target);
        if (distance > 1) {
            age = 0;
        }

        Vec3 motion = target.subtract(pos).normalize().scale(Math.min(distance, effect.followSpeed()));
        move(motion.x, motion.y, motion.z);
    }
}
