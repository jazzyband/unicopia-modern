package com.minelittlepony.unicopia.client.render.entity;

import java.util.List;
import java.util.Map;

import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.entity.mob.FairyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;

public class FairyEntityRenderer extends MobRenderer<FairyEntity, FairyEntityRenderer.State, FairyEntityRenderer.Model> {
    public FairyEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(), 0.25F);
        this.addLayer(new LightGlowFeature(this));
        shadowRadius = 0;
        shadowStrength = 0;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FairyEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.radius = 0.125F + (float)Math.sin(entity.tickCount * 0.1F) / 100F;
        state.thickness = 0.05F;
    }

    @Override
    public Identifier getTextureLocation(State entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    public static class State extends LivingEntityRenderState {
        public float radius;
        public float thickness = 0.05F;
    }

    public static class LightGlowFeature extends RenderLayer<State, Model> {
        public LightGlowFeature(RenderLayerParent<State, Model> context) {
            super(context);
        }

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector vertices, int light, State state, float limbAngle, float limbDistance) {
            matrices.pushPose();
            matrices.translate(0, 1.5, 0);

            float radius = state.radius;
            float thickness = state.thickness;
            vertices.submitCustomGeometry(matrices, RenderLayers.getMagicColored(), (pose, buffer) -> {
                PoseStack stack = new PoseStack();
                stack.mulPose(pose.pose());
                SphereModel.SPHERE.render(stack, buffer, light, OverlayTexture.NO_OVERLAY, radius - thickness, ARGB.color(CommonColors.WHITE, 127));
                SphereModel.SPHERE.render(stack, buffer, light, OverlayTexture.NO_OVERLAY, radius, ARGB.color(CommonColors.WHITE, 85));
            });

            matrices.popPose();
        }
    }

    public static class Model extends EntityModel<State> {
        public Model() {
            super(new ModelPart(List.of(), Map.of()), texture -> RenderLayers.getMagicColored());
        }
    }
}
