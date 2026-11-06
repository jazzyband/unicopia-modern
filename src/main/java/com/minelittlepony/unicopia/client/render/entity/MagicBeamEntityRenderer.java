package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;

public class MagicBeamEntityRenderer extends EntityRenderer<MagicBeamEntity, MagicBeamEntityRenderer.State> {
    private final Model model;

    public MagicBeamEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new Model(Model.getData().bakeRoot());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MagicBeamEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.scale = 1 - tickDelta/30F;
        state.yaw = entity.getViewYRot(tickDelta) * Mth.DEG_TO_RAD;
        state.pitch = -entity.getViewXRot(tickDelta) * Mth.DEG_TO_RAD;
        state.color = entity.getSpellSlot().get().map(spell -> (0x99 << 24) | spell.getTypeAndTraits().type().getColor()).orElse(0);
    }

    @Override
    protected int getBlockLightLevel(MagicBeamEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector vertexConsumers, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (state.ageInTicks < 2 && net.minecraft.client.Minecraft.getInstance().getCameraEntity().distanceToSqr(state.x, state.y, state.z) < 8) {
            return;
        }

        matrices.pushPose();
        matrices.scale(state.scale, state.scale, state.scale);

        model.setupAnim(state);
        vertexConsumers.submitCustomGeometry(matrices, state.color == 0 ? RenderLayers.getMagicColored() : RenderLayers.getMagicColored(state.color), (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            model.renderToBuffer(stack, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, CommonColors.WHITE);
        });

        matrices.popPose();
        super.submit(state, matrices, vertexConsumers, camera);
    }

    public static class State extends EntityRenderState {
        public float scale;
        public float yaw;
        public float pitch;

        public int color;
    }

    public class Model extends EntityModel<State> {

        public Model(ModelPart root) {
            super(root, texture -> RenderLayers.getMagicColored());
        }

        static LayerDefinition getData() {
            MeshDefinition data = new MeshDefinition();
            PartDefinition tree = data.getRoot();

            tree.addOrReplaceChild("beam", CubeListBuilder.create()
                    .addBox(0, 0, 0, 1, 1, 17)
                    .addBox(0, 0, 0, 1, 1, 17, new CubeDeformation(0.25F)), PartPose.ZERO);

            return LayerDefinition.create(data, 64, 64);
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            ModelPart part = root();
            part.xRot = state.pitch;
            part.yRot = state.yaw;
        }
    }
}
