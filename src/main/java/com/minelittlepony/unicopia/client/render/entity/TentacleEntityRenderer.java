package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.TentacleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class TentacleEntityRenderer extends EntityRenderer<TentacleEntity, TentacleEntityRenderer.State> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/poison_joke/tentacle.png");

    private final TentacleEntityModel model;

    public TentacleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new TentacleEntityModel(TentacleEntityModel.getTexturedModelData().bakeRoot());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TentacleEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.growth = entity.getGrowth(tickDelta);
        state.yaw = entity.getViewYRot(tickDelta);
        state.pitch = entity.getViewXRot(tickDelta);
        state.growing = state.growth < 1;
        state.animationFrame = state.ageInTicks + (entity.getUUID().getMostSignificantBits() % 100);
        state.animationTime = entity.getAnimationTimer(tickDelta);
        state.attackProgress = entity.isAttacking() ? Math.abs(Mth.sin(entity.getAttackProgress(tickDelta) * Mth.PI)) : 0;
        state.bendIntensity = 1 + entity.getAttackProgress(tickDelta) / 2F;
        state.hurting = entity.hurtTime > 0;
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector vertices, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        matrices.pushPose();
        matrices.scale(-1, -1, 1);
        float scale = state.growth;

        matrices.translate(0, -0.75F + 3F * (1 - scale), 0);
        scale = Mth.clamp(scale, 0.5F, 1);
        matrices.scale(scale, scale, scale);

        model.setupAnim(state);
        vertices.submitCustomGeometry(matrices, model.renderType(TEXTURE), (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            model.renderToBuffer(stack, buffer, state.lightCoords, OverlayTexture.pack(0, state.hurting), CommonColors.WHITE);
        });
        matrices.popPose();
        super.submit(state, matrices, vertices, camera);
    }


    @Override
    protected AABB getBoundingBoxForCulling(TentacleEntity entity, float tickDelta) {
        return super.getBoundingBoxForCulling(entity, tickDelta).inflate(10, 0, 10).expandTowards(0, 10, 0);
    }

    public static class State extends EntityRenderState {
        public float growth;
        public float yaw;
        public float pitch;
        public float animationFrame;
        public float animationTime;

        public float attackProgress;
        public float bendIntensity;

        public boolean growing;
        public boolean hurting;
    }

}