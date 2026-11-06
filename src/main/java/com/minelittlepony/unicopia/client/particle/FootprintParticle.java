package com.minelittlepony.unicopia.client.particle;

import org.joml.Quaternionf;

import com.minelittlepony.unicopia.particle.FootprintParticleEffect;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;

public class FootprintParticle extends SingleQuadParticle {
    // specter

    public FootprintParticle(FootprintParticleEffect effect, SpriteSet provider, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, 0, 0, 0, provider.get(world.getRandom()));
        setParticleSpeed(0, 0, 0);
        this.roll = effect.yaw() * Mth.DEG_TO_RAD;
        this.quadSize = 0.2F;
        this.lifetime = 1000;
        this.gravity = 1;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float tickDelta) {
        // Rotate the quad onto the ground, then turn it around the vertical axis.
        Quaternionf rotation = new Quaternionf().rotateY(roll).rotateX(Mth.HALF_PI);
        float originalAlpha = alpha;
        alpha *= 1 - (float)age / lifetime;
        extractRotatedQuad(state, camera, rotation, tickDelta);
        alpha = originalAlpha;
    }

}
