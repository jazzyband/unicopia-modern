package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.Feature;

public class HornFeatureRenderer<E extends HumanoidRenderState> implements AccessoryFeatureRenderer.Feature<E> {

    public static final Identifier TEXTURE = Unicopia.id("textures/models/horn/unicorn.png");

    private final HornModel model;

    private final RenderLayerParent<E, ? extends HumanoidModel<E>> context;

    public HornFeatureRenderer(RenderLayerParent<E, ? extends HumanoidModel<E>> context) {
        this.context = context;
        model = new HornModel(HornModel.getData(CubeDeformation.NONE).bakeRoot());
    }

    protected boolean canRender(E entity) {
        return entity instanceof AvatarRenderState
                && CasterState.of(entity).species.physical().canCast()
                && CasterState.of(entity).skinFeatures.showHorn();
    }

    @Override
    public void render(PoseStack stack, SubmitNodeCollector renderContext, int lightUv, E entity, float limbDistance, float limbAngle) {
        if (canRender(entity)) {
            model.setAngles(context.getModel());
            model.setState(false);
            RenderGeometry.submitModel(stack, renderContext, model, RenderTypes.armorCutoutNoCull(TEXTURE), lightUv, OverlayTexture.NO_OVERLAY, CommonColors.WHITE);

            CasterState state = CasterState.of(entity);
            int color = state.activeAbility.color;
            if (color == 0) {
                color = state.activeMagicColor;
            }

            if (color != 0) {
                model.setState(true);
                RenderGeometry.submitModel(stack, renderContext, model, RenderLayers.getMagicColored((0x99 << 24) | color), lightUv, OverlayTexture.NO_OVERLAY, CommonColors.WHITE);
            }
        }
    }

    public static class HornModel extends Model {
        public HornModel(ModelPart tree) {
            super(tree.getChild(PartNames.HEAD), texture -> RenderTypes.entityTranslucent((Identifier)texture));
        }

        public static LayerDefinition getData(CubeDeformation dilation) {
            MeshDefinition data = new MeshDefinition();
            PartDefinition root = data.getRoot();

            PartDefinition head = root.addOrReplaceChild(PartNames.HEAD, CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4, -8, -4, 8, 8, 8, dilation), PartPose.ZERO);
            head.addOrReplaceChild("horn", CubeListBuilder.create()
                        .texOffs(0, 3)
                        .addBox(-0.5F, -12, -0.5F, 1, 4, 1, dilation), PartPose.rotation(29 * Mth.DEG_TO_RAD, 0, 0));
            head.addOrReplaceChild("magic", CubeListBuilder.create()
                    .texOffs(0, 3)
                    .addBox(-0.5F, -12, -0.5F, 1, 4, 1, dilation.extend(0.5F)), PartPose.rotation(29 * Mth.DEG_TO_RAD, 0, 0));

            return LayerDefinition.create(data, 64, 64);
        }

        public void setAngles(HumanoidModel<?> biped) {
            root().loadPose(biped.getHead().storePose());
        }

        public void setState(boolean magic) {
            ModelPart part = root();
            part.skipDraw = true;
            part.getChild("horn").visible = !magic;
            part.getChild("magic").visible = magic;
        }
    }
}
