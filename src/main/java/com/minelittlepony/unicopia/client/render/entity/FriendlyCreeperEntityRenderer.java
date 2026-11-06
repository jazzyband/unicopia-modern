package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.FriendlyCreeperEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;

public class FriendlyCreeperEntityRenderer extends MobRenderer<FriendlyCreeperEntity, CreeperRenderState, FriendlyCreeperEntityRenderer.Model> {
    private static final Identifier FRIENDLY_TEXTURE = Unicopia.id("textures/entity/creeper/friendly.png");
    private static final Identifier UNFIRENDLY_TEXTURE = Identifier.withDefaultNamespace("textures/entity/creeper/creeper.png");

    public FriendlyCreeperEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(ModelLayers.CREEPER)), 0.5f);
        addLayer(new ChargeFeature(this, context.getModelSet()));
    }

    @Override
    public CreeperRenderState createRenderState() {
        return new CreeperRenderState();
    }

    @Override
    public void extractRenderState(FriendlyCreeperEntity entity, CreeperRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.swelling = entity.getClientFuseTime(tickDelta);
        state.isPowered = entity.isCharged();
        state.isFullyFrozen = entity.isConverting();
    }

    @Override
    protected void scale(CreeperRenderState state, PoseStack matrixStack) {
        float g = state.swelling;
        float h = 1.0f + Mth.sin(g * 100.0f) * g * 0.01f;
        g = Mth.clamp(g, 0.0f, 1.0f);
        g *= g;
        g *= g;
        float i = (1.0f + g * 0.4f) * h;
        float j = (1.0f + g * 0.1f) / h;
        matrixStack.scale(i, j, i);
    }

    @Override
    protected void setupRotations(CreeperRenderState state, PoseStack matrices, float animationProgress, float scale) {
        super.setupRotations(state, matrices, animationProgress, scale);
        if (state.hasPose(Pose.SITTING)) {
            matrices.translate(0, -0.25, 0);
        }
    }

    @Override
    protected float getWhiteOverlayProgress(CreeperRenderState state) {
        float f = state.swelling;
        return (int)(f * 10) % 2 == 0 ? 0 : Mth.clamp(f, 0.5F, 1);
    }

    @Override
    public Identifier getTextureLocation(CreeperRenderState state) {
        return state.isFullyFrozen ? UNFIRENDLY_TEXTURE : FRIENDLY_TEXTURE;
    }

    public static class Model extends CreeperModel {
        private final ModelPart leftHindLeg;
        private final ModelPart rightHindLeg;
        private final ModelPart leftFrontLeg;
        private final ModelPart rightFrontLeg;
        public Model(ModelPart root) {
            super(root);
            this.rightHindLeg = root.getChild(PartNames.RIGHT_HIND_LEG);
            this.leftHindLeg = root.getChild(PartNames.LEFT_HIND_LEG);
            this.rightFrontLeg = root.getChild(PartNames.RIGHT_FRONT_LEG);
            this.leftFrontLeg = root.getChild(PartNames.LEFT_FRONT_LEG);
        }

        @Override
        public void setupAnim(CreeperRenderState state) {
            super.setupAnim(state);
            if (state.hasPose(Pose.SITTING)) {
                float legSpread = 0.001F;
                leftHindLeg.z -= 3;
                leftHindLeg.xRot = Mth.HALF_PI;
                leftHindLeg.yRot = legSpread;
                rightHindLeg.z -= 3;
                rightHindLeg.xRot = Mth.HALF_PI;
                rightHindLeg.yRot = -legSpread;
                leftFrontLeg.z += 3;
                leftFrontLeg.xRot = -Mth.HALF_PI;
                leftFrontLeg.yRot = -legSpread;
                rightFrontLeg.z += 3;
                rightFrontLeg.xRot = -Mth.HALF_PI;
                rightFrontLeg.yRot = legSpread;
            }
        }
    }

    public static class ChargeFeature extends EnergySwirlLayer<CreeperRenderState, Model> {
        private static final Identifier SKIN = Identifier.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");
        private final Model model;

        public ChargeFeature(RenderLayerParent<CreeperRenderState, Model> context, EntityModelSet models) {
            super(context);
            model = new Model(models.bakeLayer(ModelLayers.CREEPER_ARMOR));
        }

        @Override
        protected boolean isPowered(CreeperRenderState creeperEntityRenderState) {
            return creeperEntityRenderState.isPowered;
        }

        @Override
        protected float xOffset(float partialAge) {
            return partialAge * 0.01f;
        }

        @Override
        protected Identifier getTextureLocation() {
            return SKIN;
        }

        @Override
        protected Model model() {
            return this.model;
        }
    }
}
