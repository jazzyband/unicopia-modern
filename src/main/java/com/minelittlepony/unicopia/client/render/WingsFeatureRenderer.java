package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

public class WingsFeatureRenderer<S extends HumanoidRenderState> implements AccessoryFeatureRenderer.Feature<S> {

    protected static final int FEATHER_COUNT = 8;

    private static final Identifier PEGASUS_WINGS = Unicopia.id("textures/models/wings/pegasus.png");
    private static final Identifier PEGASUS_WINGS_OVERLAY = Unicopia.id("textures/models/wings/pegasus_overlay.png");

    private final WingsModel model;

    private final RenderLayerParent<S, ? extends HumanoidModel<S>> context;

    public WingsFeatureRenderer(RenderLayerParent<S, ? extends HumanoidModel<S>> context) {
        this.context = context;
        this.model = new WingsModel(createModel(CubeDeformation.NONE).bakeRoot());
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector renderContext, int lightUv, S entity, float limbDistance, float limbAngle) {
        if (canRender(entity)) {
            model.setAngles(CasterState.of(entity), context.getModel());
            RenderGeometry.submitModel(matrices, renderContext, model, getLayer(getTexture(entity)), lightUv, OverlayTexture.NO_OVERLAY, CommonColors.WHITE);

            Identifier overlayTexture = getOverlayTexture(entity);
            if (overlayTexture != null) {
                RenderGeometry.submitModel(matrices, renderContext, model, getLayer(overlayTexture), lightUv, OverlayTexture.NO_OVERLAY, CommonColors.WHITE);
            }
        }
    }

    protected RenderType getLayer(Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    protected boolean canRender(S state) {
        return state instanceof AvatarRenderState
                && CasterState.of(state).species.physical().flightType() == FlightType.AVIAN
                && CasterState.of(state).species.physical() != Race.BAT
                && CasterState.of(state).skinFeatures.showWings()
                && !CasterState.of(state).pegasusAmulet;
    }

    protected Identifier getTexture(S state) {
        return PEGASUS_WINGS;
    }

    @Nullable
    protected Identifier getOverlayTexture(S state) {
        return PEGASUS_WINGS_OVERLAY;
    }

    private LayerDefinition createModel(CubeDeformation dilation) {
        MeshDefinition data = new MeshDefinition();
        createWing("left_wing", data.getRoot(), dilation, -1);
        createWing("right_wing", data.getRoot(), dilation, 1);
        return LayerDefinition.create(data, 24, 23);
    }

    protected void createWing(String name, PartDefinition parent, CubeDeformation dilation, int k) {
        PartDefinition base = parent.addOrReplaceChild(name,
                CubeListBuilder.create().addBox(0, 0, 0, 2, 10, 2, dilation),
                PartPose.offset(k * 2, 2, 2 + k * 0.5F));

        for (int i = 0; i < FEATHER_COUNT; i++) {
            int texX = (i % 2) * 8;
            int featherLength = 21 - i * 2;
            base.addOrReplaceChild("feather_" + i,
                    CubeListBuilder.create()
                        .texOffs(8 + texX, 0)
                        .addBox(-k * (i % 2) / 90F, 0, 0, 2, featherLength, 2, dilation),
                    PartPose.offset(0, 9, 0));
        }
    }

    private static class WingsModel extends Model {
        private final Wing leftWing;
        private final Wing rightWing;

        public WingsModel(ModelPart tree) {
            super(tree, texture -> RenderTypes.entityTranslucent((Identifier)texture));
            leftWing = new Wing(tree.getChild("left_wing"), -1);
            rightWing = new Wing(tree.getChild("right_wing"), 1);
        }

        public void setAngles(CasterState entity, HumanoidModel<?> biped) {
            root.loadPose(biped.body.storePose());
            leftWing.setAngles(entity);
            rightWing.setAngles(entity);
        }

        static class Wing {
            final ModelPart base;

            final ModelPart[] feathers = new ModelPart[FEATHER_COUNT];

            final int k;

            Wing(ModelPart tree, int k) {
                this.k = k;
                base = tree;
                for (int i = 0; i < feathers.length; i++) {
                    feathers[i] = base.getChild("feather_" + i);
                }
            }

            void setAngles(CasterState entity) {
                float spreadAmount = entity.wingsAngle;

                base.xRot = 1.5F + 0.8F - spreadAmount / 9F;
                base.yRot = k * (0.8F + spreadAmount / 3F);

                spreadAmount /= 7F;

                final float ratio = 4F;

                for (int i = 0; i < feathers.length; i++) {

                    float spread = i/ratio + 1.5F;
                    spread -= spreadAmount * ratio;
                    spread += spreadAmount * i / ratio;

                    feathers[i].xRot = -spread;
                    feathers[i].yRot = k * 0.3F;
                }
            }
        }
    }
}
