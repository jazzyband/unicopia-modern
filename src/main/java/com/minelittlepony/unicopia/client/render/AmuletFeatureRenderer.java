package com.minelittlepony.unicopia.client.render;

import java.util.HashMap;
import java.util.Map;

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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Item;

public class AmuletFeatureRenderer<E extends HumanoidRenderState> implements AccessoryFeatureRenderer.Feature<E> {

    private final AmuletModel model;

    private final Map<ResourceKey<Item>, Identifier> textures = new HashMap<>();

    private final RenderLayerParent<E, ? extends HumanoidModel<E>> context;

    public AmuletFeatureRenderer(RenderLayerParent<E, ? extends HumanoidModel<E>> context) {
        this.context = context;
        this.model = new AmuletModel(AmuletModel.getData(new CubeDeformation(0.3F)).bakeRoot());
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector renderContext, int lightUv, E entity, float limbDistance, float limbAngle) {

        CasterState casterState = CasterState.of(entity);

        if (!casterState.amulet.empty) {
            Identifier texture = textures.computeIfAbsent(casterState.amulet.key, key -> key.identifier().withPath(p -> "textures/models/armor/" + p + ".png"));


            model.root().resetPose();
            if (context.getModel() instanceof HumanoidModel) {
                model.setAngles(context.getModel());
            }
            RenderGeometry.submitModel(matrices, renderContext, model, RenderTypes.armorCutoutNoCull(texture), lightUv, OverlayTexture.NO_OVERLAY, CommonColors.WHITE);
        }
    }

    public static class AmuletModel extends Model {

        public AmuletModel(ModelPart tree) {
            super(tree.getChild("amulet"), texture -> RenderTypes.entityTranslucent((Identifier)texture));
        }

        public static LayerDefinition getData(CubeDeformation dilation) {
            MeshDefinition data = new MeshDefinition();
            PartDefinition root = data.getRoot();

            root.addOrReplaceChild("amulet", CubeListBuilder.create().addBox(-4, 0, -2, 8, 12, 4, dilation), PartPose.ZERO);

            return LayerDefinition.create(data, 64, 32);
        }

        public void setAngles(HumanoidModel<?> biped) {
            root().loadPose(biped.body.storePose());
        }
    }
}
