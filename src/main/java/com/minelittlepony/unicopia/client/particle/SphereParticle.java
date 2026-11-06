package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.util.ColorHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import static net.minecraft.util.ARGB.*;

public class SphereParticle extends Particle implements CustomRenderableParticle {
    static final int TRANSLUCENT_WHITE = color((int)(0.8F * 255), CommonColors.WHITE);

    protected float prevRadius;
    protected float radius;
    protected float rCol;
    protected float gCol;
    protected float bCol;
    protected float alpha;

    protected int steps;
    protected float lerpIncrement;
    protected float toRadius;

    public SphereParticle(SphereParticleEffect parameters, ClientLevel w, double x, double y, double z, double vX, double vY, double vZ) {
        this(parameters, w, x, y, z);

        this.xd = vX;
        this.yd = vY;
        this.zd = vZ;
    }

    public SphereParticle(SphereParticleEffect parameters, ClientLevel w, double x, double y, double z) {
        super(w, x, y, z);
        this.radius = parameters.radius();
        this.rCol = parameters.color().x / 255F;
        this.gCol = parameters.color().y / 255F;
        this.bCol = parameters.color().z / 255F;
        this.alpha = parameters.alpha();

        setLifetime(10);
    }

    @Override
    public ParticleRenderType getGroup() {
        return CustomParticleGroup.RENDER_TYPE;
    }

    @Override
    public void tick() {
        super.tick();

        radius *= 0.9998281;
    }

    @Override
    public void submitCustomGeometry(SubmitNodeCollector collector, Camera camera, float tickDelta) {

        if (alpha <= 0 || radius <= 0) {
            return;
        }

        float[] color = ColorHelper.changeSaturation(rCol, gCol, bCol, 4);

        PoseStack matrices = new PoseStack();

        matrices.pushPose();
        matrices.translate(
                Mth.lerp(tickDelta, xo, x) - camera.position().x,
                Mth.lerp(tickDelta, yo, y) - camera.position().y,
                Mth.lerp(tickDelta, zo, z) - camera.position().z
        );

        float scale = Mth.lerp(tickDelta, prevRadius, radius);

        int light = getLightCoords(tickDelta);
        collector.submitCustomGeometry(new PoseStack(),
                RenderLayers.getMagicColored(net.minecraft.util.ARGB.colorFromFloat(color[0], color[1], color[2], alpha / 3F)),
                (pose, buffer) -> renderModel(matrices, buffer, scale, tickDelta, light));

        prevRadius = radius;

    }

    protected void renderModel(PoseStack matrices, VertexConsumer buffer, float lerpedRad, float tickDelta, int light) {
        float thickness = 0.05F;
        SphereModel.SPHERE.render(matrices, buffer, light, 1, lerpedRad + thickness, TRANSLUCENT_WHITE);
        SphereModel.SPHERE.render(matrices, buffer, light, 1, lerpedRad - thickness, CommonColors.WHITE);
    }
}

