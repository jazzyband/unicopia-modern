package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

public class FloatingBubbleParticle extends SingleQuadParticle {
    public FloatingBubbleParticle(ParticleOptions effect, SpriteSet provider, ClientLevel clientWorld, double x, double y, double z, double dX, double dY, double dZ) {
        super(clientWorld, x, y, z, dX, dY, dZ, provider.get(clientWorld.getRandom()));
        scale(clientWorld.getRandom().triangle(1F, 0.5F));
        this.xd *= -0.1F;
        this.yd *= -0.1F;
        this.zd *= -0.1F;
        this.lifetime *= 3;
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Override
    public void remove() {
        super.remove();
        level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0, 0);
    }
}
