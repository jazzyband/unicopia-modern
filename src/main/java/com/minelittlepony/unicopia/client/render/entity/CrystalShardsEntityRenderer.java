package com.minelittlepony.unicopia.client.render.entity;

import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.joml.Quaternionf;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.CrystalShardsEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class CrystalShardsEntityRenderer extends EntityRenderer<CrystalShardsEntity, CrystalShardsEntityRenderer.State> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/crystal_shards/normal.png");
    private static final Identifier[] CORRUPTED = List.of("corrupt", "dark", "darker").stream()
            .map(name -> Unicopia.id("textures/entity/crystal_shards/" + name + ".png"))
            .toArray(Identifier[]::new);

    private final CrystalShardsEntityModel model;

    public CrystalShardsEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new CrystalShardsEntityModel(CrystalShardsEntityModel.getTexturedModelData().bakeRoot());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CrystalShardsEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.shaking = entity.isShaking();
        state.destructionStage = FloatingArtefactEntityRenderer.getDestructionStage(entity);
        state.rotation = entity.getAttachmentFace().getRotation();
        state.growth = entity.getGrowth(tickDelta);
        state.yaw = entity.getViewYRot(tickDelta);
        state.corruption = entity.isCorrupt() ? (int)(Math.abs(entity.getUUID().getMostSignificantBits()) % CORRUPTED.length) : -1;
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector vertices, CameraRenderState camera) {
        matrices.pushPose();
        matrices.rotate(state.rotation);
        matrices.scale(-1, -1, 1);

        matrices.scale(state.growth, state.growth, state.growth);
        matrices.rotate(Axis.YP.rotationDegrees(state.yaw));

        vertices.submitCustomGeometry(matrices, model.renderType(state.corruption > -1 ? CORRUPTED[state.corruption] : TEXTURE), (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            model.setupAnim(state);
            model.renderToBuffer(stack, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, CommonColors.WHITE);
        });
        matrices.popPose();
        super.submit(state, matrices, vertices, camera);
    }

    public static class State extends EntityRenderState {
        public boolean shaking;
        public int destructionStage;
        public Quaternionf rotation;
        public float growth;
        public float yaw;

        public int corruption = -1;
    }
}
