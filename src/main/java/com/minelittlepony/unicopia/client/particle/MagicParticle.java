package com.minelittlepony.unicopia.client.particle;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

public class MagicParticle extends SingleQuadParticle {
    private final double startX;
    private final double startY;
    private final double startZ;

    MagicParticle(ParticleOptions effect, SpriteSet provider, ClientLevel w, double x, double y, double z, double vX, double vY, double vZ, Vector3f color, float alpha) {
        super(w, x, y, z, provider.get(w.getRandom()));

        xd = vX;
        yd = vY;
        zd = vZ;
        startX = x + random.nextGaussian()/3;
        startY = y + random.nextGaussian()/3;
        startZ = z + random.nextGaussian()/3;
        quadSize = random.nextFloat() * 0.12F;
        lifetime = (int)(Math.random() * 10) + 20;

        rCol = color.x;
        gCol = color.y;
        bCol = color.z;
        this.alpha = alpha;
    }

    public MagicParticle(MagicParticleEffect effect, SpriteSet provider, ClientLevel w, double x, double y, double z, double vX, double vY, double vZ) {
        this(effect, provider, w, x, y, z, vX, vY, vZ, effect.getColor(w.getRandom()), 0.7F);
    }

    @Override
    protected Layer getLayer() {
       return Layer.TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float tickDelta) {
       float shrink = (age + tickDelta) / lifetime;
       return this.quadSize * (1 - shrink * shrink);
    }

    @Override
    public int getLightCoords(float tint) {
        int light = super.getLightCoords(tint);
        float timer = (float)age / (float)lifetime;

        int v = light >> 16 & 255;
        v = (int)Math.min(v + (timer * timer * timer * timer) * 240, 240);

        return (light & 255) | v << 16;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;

        if (age++ >= lifetime) {
            remove();
        } else {
            float timer = (float)age / (float)lifetime;

            timer += 1 - (timer * timer) * 2;

            x = startX + xd * timer;
            y = startY + yd;
            z = startZ + zd * timer;
        }
    }

}
