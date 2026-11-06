package com.minelittlepony.unicopia.client.particle;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.VertexConsumer;

public abstract class OrientedBillboardParticle extends AbstractBillboardParticle {

    protected boolean fixed;
    protected Quaternionf rotation = new Quaternionf(0, 0, 0, 1);

    public OrientedBillboardParticle(OrientedBillboardParticleEffect effect, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);

        fixed = effect.fixed();
        if (fixed) {
            rotation.mul(Axis.YP.rotationDegrees(effect.pitch()));
            rotation.mul(Axis.XP.rotationDegrees(180 - effect.yaw()));
        }
    }

    @Override
    public void submitCustomGeometry(SubmitNodeCollector collector, Camera camera, float tickDelta) {
        if (!fixed) {
            rotation = camera.rotation();
        }
        super.submitCustomGeometry(collector, camera, tickDelta);
    }

    @Override
    protected void renderQuads(VertexConsumer te, float x, float y, float z, float tickDelta) {
        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1, -1, 0),
                new Vector3f(-1,  1, 0),
                new Vector3f( 1,  1, 0),
                new Vector3f( 1, -1, 0)
        };
        float scale = getScale(tickDelta);

        for(int k = 0; k < 4; ++k) {
           Vector3f corner = corners[k];
           corner.rotate(rotation);
           corner.mul(scale);
           corner.add(x, y, z);
        }

        float alpha = this.alpha * (1 - ((float)age / lifetime));

        renderQuad(te, corners, alpha, tickDelta);
    }

}
