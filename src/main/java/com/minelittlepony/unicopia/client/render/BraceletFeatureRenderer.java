package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState.BangleState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.levelgen.feature.Feature;

public class BraceletFeatureRenderer<S extends HumanoidRenderState, E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<S> {

    public static final Identifier TEXTURE = Unicopia.id("textures/models/armor/bracelet.png");

    private final BraceletModel steveModel;
    private final BraceletModel alexModel;

    private final RenderLayerParent<S, ? extends HumanoidModel<S>> context;

    public BraceletFeatureRenderer(RenderLayerParent<S, ? extends HumanoidModel<S>> context) {
        this.context = context;
        CubeDeformation dilation = new CubeDeformation(0.3F);
        steveModel = new BraceletModel(BraceletModel.getData(dilation, false, 0, 0, 0).bakeRoot());
        alexModel = new BraceletModel(BraceletModel.getData(dilation, true, 0, 0, 0).bakeRoot());
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, S entity, float limbAngle, float limbDistance) {
        CasterState caster = CasterState.of(entity);
        renderBangleThirdPerson(caster.mainhandBangle, matrices, vertexConsumers, light, entity, limbDistance, limbAngle, entity.mainArm);
        renderBangleThirdPerson(caster.offhandBangle, matrices, vertexConsumers, light, entity, limbDistance, limbAngle, entity.mainArm.getOpposite());
    }

    private void renderBangleThirdPerson(BangleState state, PoseStack stack, SubmitNodeCollector renderContext, int lightUv, S entity, float limbDistance, float limbAngle, HumanoidArm mainArm) {
        if (state.empty) {
            return;
        }
        int j = state.color;

        boolean alex = entity instanceof AvatarRenderState s && s.skin.model() == net.minecraft.world.entity.player.PlayerModelType.SLIM;

        BraceletModel model = alex ? alexModel : steveModel;
        boolean isLeft = mainArm == HumanoidArm.LEFT;

        if (entity instanceof ArmorStandRenderState stand) {
            ModelPart arm = isLeft ? context.getModel().leftArm : context.getModel().rightArm;
            arm.visible = true;
            RenderGeometry.submit(stack, renderContext, context.getModel().renderType(context instanceof LivingEntityRenderer renderer ? renderer.getTextureLocation(stand) : ArmorStandRenderer.DEFAULT_SKIN_LOCATION),
                    (pose, consumer) -> arm.render(pose, consumer, lightUv, OverlayTexture.NO_OVERLAY, CommonColors.WHITE));
        }

        model.setAngles(context.getModel());
        model.setVisible(mainArm);
        RenderGeometry.submitModel(stack, renderContext, model, RenderTypes.armorCutoutNoCull(TEXTURE), state.glowing ? LightCoordsUtil.FULL_BRIGHT : lightUv, OverlayTexture.NO_OVERLAY, j);
    }

    @Override
    public void renderArm(PoseStack stack, SubmitNodeCollector renderContext, int lightUv, S entity, ModelPart armModel, HumanoidArm arm) {
        var state = CasterState.of(entity);
        var bangle = arm == entity.mainArm ? state.mainhandBangle : state.offhandBangle;
        if (!bangle.empty) {
            if (state.ponified) {
                stack.translate(arm == HumanoidArm.LEFT ? 0.06 : -0.06, 0.3, 0);
            } else {
                stack.translate(0, -0.1, 0);
            }

            boolean alex = entity instanceof AvatarRenderState s && s.skin.model() == net.minecraft.world.entity.player.PlayerModelType.SLIM;
            BraceletModel model = alex ? alexModel : steveModel;
            model.setAngles(context.getModel());
            model.setVisible(arm);
            RenderGeometry.submitModel(stack, renderContext, model, RenderTypes.armorCutoutNoCull(TEXTURE), bangle.glowing ? LightCoordsUtil.FULL_BRIGHT : lightUv, OverlayTexture.NO_OVERLAY, bangle.color);
        }
    }

    public static class BraceletModel extends Model {

        private final ModelPart leftArm;
        private final ModelPart rightArm;

        public BraceletModel(ModelPart tree) {
            super(tree, texture -> RenderTypes.entityTranslucent((Identifier)texture));
            this.leftArm = tree.getChild(PartNames.LEFT_ARM);
            this.rightArm = tree.getChild(PartNames.RIGHT_ARM);
        }

        public static LayerDefinition getData(CubeDeformation dilation, boolean alex, int x, int y, int z) {
            MeshDefinition data = new MeshDefinition();
            PartDefinition root = data.getRoot();

            root.addOrReplaceChild(PartNames.RIGHT_ARM,
                    CubeListBuilder.create()
                        .texOffs(0, alex ? 6 : 0)
                        .addBox((alex ? -2 : -3) + x, 7 + y, -2 + z, alex ? 3 : 4, 2, 4, dilation), PartPose.ZERO);
            root.addOrReplaceChild(PartNames.LEFT_ARM,
                    CubeListBuilder.create().mirror()
                        .texOffs(0, alex ? 6 : 0)
                        .addBox(-1 - x, 7 + y, -2 + z, alex ? 3 : 4, 2, 4, dilation), PartPose.ZERO);

            return LayerDefinition.create(data, 64, 32);
        }

        public void setAngles(HumanoidModel<?> biped) {
            leftArm.loadPose(biped.leftArm.storePose());
            rightArm.loadPose(biped.rightArm.storePose());
        }

        public void setVisible(HumanoidArm arm) {
            leftArm.visible = arm == HumanoidArm.LEFT;
            rightArm.visible = arm == HumanoidArm.RIGHT;
        }
    }
}

