package com.minelittlepony.unicopia.client.particle;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.client.render.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.particle.ParticleRenderType;

public abstract class AbstractGeometryBasedParticle extends Particle implements CustomRenderableParticle {

    protected float scale = 1;
    protected float rCol = 1;
    protected float gCol = 1;
    protected float bCol = 1;
    protected float alpha = 1;

    public AbstractGeometryBasedParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Override
    public ParticleRenderType getGroup() {
        return CustomParticleGroup.RENDER_TYPE;
    }

    protected final void renderQuad(PoseStack matrices, VertexConsumer buffer, RenderUtil.Vertex[] corners, float alpha, float tickDelta) {
        int light = getLightCoords(tickDelta);
        for (RenderUtil.Vertex corner : corners) {
            var position = corner.position(matrices.last().pose());
            buffer.addVertex(position.x, position.y, position.z).setUv(corner.texture().x, corner.texture().y).setColor(rCol, gCol, bCol, alpha).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0);
        }
    }

    protected final void renderQuad(VertexConsumer buffer, RenderUtil.Vertex[] corners, float alpha, float tickDelta) {
        quad(buffer, corners, alpha, tickDelta, getLightCoords(tickDelta));
    }

    protected final void quad(VertexConsumer buffer, RenderUtil.Vertex[] corners, float alpha, float tickDelta, int light) {
        for (RenderUtil.Vertex corner : corners) {
            buffer.addVertex(corner.position().x, corner.position().y, corner.position().z).setUv(corner.texture().x, corner.texture().y).setColor(rCol, gCol, bCol, alpha).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0);
        }
    }

    protected final void renderQuad(VertexConsumer buffer, Vector3f[] corners, float alpha, float tickDelta) {
        int light = getLightCoords(tickDelta);
        // Match the billboard corner order; lightning's position/color buffer ignores these attributes.
        for (int i = 0; i < corners.length; i++) {
            Vector3f corner = corners[i];
            buffer.addVertex(corner.x, corner.y, corner.z).setUv(i % 4 < 2 ? 0 : 1, i % 4 == 0 || i % 4 == 3 ? 1 : 0).setColor(rCol, gCol, bCol, alpha).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0);
        }
    }

    public float getScale(float tickDelta) {
       return scale;
    }

    @Override
    public Particle scale(float scale) {
       this.scale = scale;
       return super.scale(scale);
    }
}
