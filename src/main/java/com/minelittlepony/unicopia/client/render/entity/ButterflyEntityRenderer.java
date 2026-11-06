package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class ButterflyEntityRenderer extends MobRenderer<ButterflyEntity, ButterflyEntityRenderer.State, ButterflyEntityRenderer.ButterflyEntityModel> {
    public ButterflyEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyEntityModel(ButterflyEntityModel.getData().bakeRoot()), 0.25F);
        shadowRadius = 0.2F;
        shadowStrength = 0.3F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ButterflyEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.resting = entity.isResting();
        state.variant = entity.getVariant();
        BlockPos pos = entity.blockPosition();
        state.translucent = getBlockLightLevel(entity, pos) < 7 && getSkyLightLevel(entity, pos) < 15;
        state.bodyPitch = state.resting ? 0.8F : ((float)Math.PI / 4) + Mth.cos(state.ageInTicks * 0.1F) * 0.15F;
        state.wingAngle = state.resting ? Mth.cos((state.ageInTicks + (1 + entity.getId()) % 2) / 20) * (float)Math.PI / 6 + 0.7F : Mth.cos(state.ageInTicks) * (float)Math.PI / 4;
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return state.variant.getSkin();
    }

    @Override
    protected void scale(State state, PoseStack matrices) {
        matrices.scale(0.35F, 0.35F, 0.35F);
        matrices.translate(0.5F, 0, -0.5F);
    }

    @Override
    protected void setupRotations(State state, PoseStack matrices, float animationProgress, float bodyYaw) {
        if (!state.resting) {
            matrices.translate(0, Mth.cos(state.ageInTicks / 3F) / 10F, 0);
        }
        super.setupRotations(state, matrices, animationProgress, bodyYaw);
    }

    @Override
    @Nullable
    protected RenderType getRenderType(State state, boolean showBody, boolean translucent, boolean showOutline) {
        if (showBody && !translucent && state.translucent) {
            return RenderTypes.entityTranslucent(getTextureLocation(state));
        }
        return super.getRenderType(state, showBody, translucent, showOutline);
    }

    @Override
    protected int getSkyLightLevel(ButterflyEntity entity, BlockPos pos) {
        return (int)(super.getSkyLightLevel(entity, pos) * (entity.level() instanceof ClientLevel w ? w.getSkyDarken() : 1));
    }

    public static class State extends LivingEntityRenderState {
        public boolean resting;
        public ButterflyEntity.Variant variant = ButterflyEntity.Variant.BUTTERFLY;

        public boolean translucent;
        public float wingAngle;
        public float bodyPitch;
    }

    public static class ButterflyEntityModel extends EntityModel<State> {
        private final ModelPart leftWing;
        private final ModelPart rightWing;

        public ButterflyEntityModel(ModelPart tree) {
            super(tree, RenderTypes::entityTranslucent);
            tree.x = -10;
            tree.y = 12;
            leftWing = tree.getChild("left_wing");
            rightWing = tree.getChild("right_wing");
        }

        static LayerDefinition getData() {
            MeshDefinition data = new MeshDefinition();
            PartDefinition tree = data.getRoot();

            tree.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(42, 0).addBox(-13, -5, 0, 10, 19, 1), PartPose.rotation(0, 0, -0.2F))
                .addOrReplaceChild("right_wing_outer", CubeListBuilder.create().texOffs(24, 16).addBox(0, 0, 0, 10, 12, 1), PartPose.offsetAndRotation(-13, 10, 0.1F, 0, 0, -0.2F));

            tree.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(42, 0).mirror().addBox(2, -5, 0, 10, 19, 1), PartPose.rotation(0, 0, 0.2F))
                .addOrReplaceChild("left_wing_outer", CubeListBuilder.create().texOffs(24, 16).addBox(0, 0, 0, 10, 12, 1), PartPose.offsetAndRotation(2, 10, 0.1F, 0, 0, 0.2F));

            return LayerDefinition.create(data, 64, 64);
        }

        @Override
        public void setupAnim(State state) {
            root().xRot = state.bodyPitch;
            leftWing.yRot = -state.wingAngle;
            rightWing.yRot = state.wingAngle;
        }
    }
}
