package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import org.joml.Vector3f;

public class ChangelingMagicParticle extends MagicParticle {

    private final SpriteSet provider;

    public ChangelingMagicParticle(ParticleOptions effect, SpriteSet provider, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
        super(effect, provider, world, x, y, z, dx, dy, dz, nextColor(world.getRandom().nextFloat() * 0.6F + 0.4F), 1);
        this.provider = provider;
    }

    static Vector3f nextColor(float intensity) {
        return new Vector3f(intensity * 0.5F, intensity, intensity * 0.4F);
    }

    @Override
    public void tick() {
        setSpriteFromAge(provider);
        super.tick();
    }
}
