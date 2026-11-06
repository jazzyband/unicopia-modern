package com.minelittlepony.unicopia.client.render.entity;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.StormCloudEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.RandomSource;

public class StormCloudEntityRenderer extends EntityRenderer<StormCloudEntity, StormCloudEntityRenderer.State> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/storm_cloud.png");
    private static final int DEFAULT_COLOR = ARGB.color(CommonColors.WHITE, (int)(255 * 0.9F));

    private final RandomSource rng = RandomSource.create(0);
    private final StormCloudEntityModel model = new StormCloudEntityModel(StormCloudEntityModel.getTexturedModelData().bakeRoot());

    public StormCloudEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StormCloudEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.yaw = entity.getViewYRot(tickDelta);
        state.scale = entity.getSize(tickDelta);
        state.stormy = entity.isStormy();

        rng.setSeed(entity.getId());
        state.puffLocations = new Vector3f[rng.nextInt(7)];

        for (int i = 0; i < state.puffLocations.length; i++) {
            state.puffLocations[i] = new Vector3f(
                    (float)rng.nextGaussian(),
                    (float)rng.nextGaussian(),
                    (float)rng.nextGaussian()
            ).mul(16);
        }
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector vertices, CameraRenderState camera) {
        matrices.pushPose();
        matrices.scale(-1, -1, 1);
        matrices.rotate(Axis.YP.rotationDegrees(state.yaw));

        matrices.scale(state.scale, state.scale, state.scale);
        matrices.translate(0, -1.45F, 0);

        int light = state.stormy ? 0 : state.lightCoords;
        vertices.submitCustomGeometry(matrices, model.renderType(TEXTURE), (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            model.setupAnim(state);
            model.renderToBuffer(stack, buffer, light, OverlayTexture.NO_OVERLAY, DEFAULT_COLOR);
            stack.pushPose();
            model.root().translateAndRotate(stack);
            for (Vector3f puffPosition : state.puffLocations) {
                model.renderPuff(puffPosition, stack, buffer, light, OverlayTexture.NO_OVERLAY, DEFAULT_COLOR);
            }
            stack.popPose();
        });
        matrices.popPose();
        super.submit(state, matrices, vertices, camera);
    }

    @Override
    public boolean shouldRender(StormCloudEntity entity, Frustum frustum, double x, double y, double z, float tickDelta) {
        return true;
    }

    public static class State extends EntityRenderState {
        public float yaw;
        public float scale;
        public boolean stormy;

        public Vector3f[] puffLocations = new Vector3f[0];
    }
}
