package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.particles.ParticleOptions;

public class RaindropsParticle extends WaterDropParticle {

    public RaindropsParticle(ParticleOptions effect, SpriteSet provider, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, provider.get(world.getRandom()));
        yd = -0.1;
        lifetime += 19;
    }

    @Override
    public void tick() {
        super.tick();

        if (onGround) {
            xd *= 0.30000001192092896D;
            yd = Math.random() * 0.20000000298023224D + 0.10000000149011612D;
            zd *= 0.30000001192092896D;
        }
    }
}
