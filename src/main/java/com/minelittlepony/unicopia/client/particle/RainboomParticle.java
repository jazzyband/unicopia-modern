package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class RainboomParticle extends OrientedBillboardParticle {
    private static final Identifier TEXTURE = Unicopia.id("textures/particles/rainboom_ring.png");

    protected float prevBaseSize = 0;
    protected float baseSize = 0;

    public RainboomParticle(OrientedBillboardParticleEffect effect, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(effect, world, x, y, z, velocityX, velocityY, velocityZ);
        setLifetime(40);
    }

    @Override
    public float getScale(float tickDelta) {
       return Mth.lerp(tickDelta, prevBaseSize, baseSize) * super.getScale(tickDelta);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public void tick() {
        super.tick();

        prevBaseSize = baseSize;
        baseSize++;

        if (age == 1) {
            level.playLocalSound(x, y, z, USounds.PARTICLE_RAINBOOM_THUNDER, SoundSource.AMBIENT, 5, 0.3F, true);
            level.playLocalSound(x, y, z, USounds.PARTICLE_RAINBOOM_REVERB, SoundSource.AMBIENT, 10, 1.3F, true);
        }
    }
}
