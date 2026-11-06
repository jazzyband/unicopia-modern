package com.minelittlepony.unicopia.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractBillboardParticle extends AbstractGeometryBasedParticle {

    public AbstractBillboardParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Override
    public void submitCustomGeometry(SubmitNodeCollector collector, Camera camera, float tickDelta) {
        Vec3 cam = camera.position();

        float renderX = (float)(Mth.lerp(tickDelta, xo, x) - cam.x());
        float renderY = (float)(Mth.lerp(tickDelta, yo, y) - cam.y());
        float renderZ = (float)(Mth.lerp(tickDelta, zo, z) - cam.z());

        collector.submitCustomGeometry(new PoseStack(), RenderTypes.entityTranslucent(getTexture()),
                (pose, vertices) -> renderQuads(vertices, renderX, renderY, renderZ, tickDelta));
    }

    protected abstract void renderQuads(VertexConsumer vertexConsumer, float x, float y, float z, float tickDelta);

    protected abstract Identifier getTexture();
}
