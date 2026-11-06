package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.IgnominiousBulbEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;

public class IgnominiousBulbEntityRenderer extends EntityRenderer<IgnominiousBulbEntity, IgnominiousBulbEntityRenderer.State> {
    private static final Identifier IDLE_TEXTURE = Unicopia.id("textures/entity/poison_joke/bulb_idle.png");
    private static final Identifier ANGRY_TEXTURE = Unicopia.id("textures/entity/poison_joke/bulb_angry.png");

    private final IgnominiousBulbEntityModel model;

    public IgnominiousBulbEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new IgnominiousBulbEntityModel(IgnominiousBulbEntityModel.getTexturedModelData().bakeRoot());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(IgnominiousBulbEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.angry = entity.isAngry();
        state.scale = entity.getScale(tickDelta);
        state.pitch = entity.getViewXRot(tickDelta);
        state.yaw = (180 + entity.getViewYRot(tickDelta)) * Mth.DEG_TO_RAD;
        state.hurting = entity.hurtTime > 0;
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector vertices, CameraRenderState camera) {
        matrices.pushPose();
        matrices.scale(-1, -1, 1);
        matrices.translate(0, -1.5F, 0);

        vertices.submitCustomGeometry(matrices, model.renderType(state.angry ? ANGRY_TEXTURE : IDLE_TEXTURE), (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            model.setupAnim(state);
            model.renderToBuffer(stack, buffer, state.lightCoords, OverlayTexture.pack(0, state.hurting), CommonColors.WHITE);
        });
        matrices.popPose();
        super.submit(state, matrices, vertices, camera);
    }

    public static class State extends EntityRenderState {
        public boolean angry;
        public boolean hurting;
        public float pitch;
        public float yaw;
        public float scale;
    }
}
