package com.minelittlepony.unicopia.client.render;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.level.levelgen.feature.Feature;

public class GlassesFeatureRenderer<S extends HumanoidRenderState, E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<S> {

    private final GlassesModel model;

    private final Map<Identifier, Identifier> textures = new HashMap<>();

    private final RenderLayerParent<S, ? extends HumanoidModel<S>> context;

    public GlassesFeatureRenderer(RenderLayerParent<S, ? extends HumanoidModel<S>> context) {
        this.context = context;
        this.model = new GlassesModel(GlassesModel.getData(CubeDeformation.NONE, -8, -4).bakeRoot());
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector renderContext, int lightUv, S entity, float limbDistance, float limbAngle) {

        ItemStack stack = CasterState.of(entity).eyewear.stack();

        if (!stack.isEmpty()) {
            Identifier texture = textures.computeIfAbsent(BuiltInRegistries.ITEM.getKey(stack.getItem()), id -> id.withPath(p -> "textures/models/armor/" + p + ".png"));


            model.setAngles(entity, context.getModel());
            RenderGeometry.submitModel(matrices, renderContext, model, RenderTypes.armorCutoutNoCull(texture), lightUv, OverlayTexture.NO_OVERLAY, CommonColors.WHITE);
        }
    }

    public static class GlassesModel extends Model {

        public GlassesModel(ModelPart tree) {
            super(tree, texture -> RenderTypes.entityTranslucent((Identifier)texture));
        }

        public static LayerDefinition getData(CubeDeformation dilation, int y, int z) {
            MeshDefinition data = new MeshDefinition();
            PartDefinition root = data.getRoot();

            root.addOrReplaceChild(PartNames.HEAD, CubeListBuilder.create().texOffs(0, 0).addBox(-4, y, z, 8, 8, 8, dilation.extend(0.2F)), PartPose.ZERO);
            root.addOrReplaceChild(PartNames.HAT, CubeListBuilder.create().texOffs(32, 0).addBox(-4, y, z, 8, 8, 8, dilation.extend(0.4F)), PartPose.ZERO);

            return LayerDefinition.create(data, 64, 32);
        }

        public void setAngles(HumanoidRenderState entity, HumanoidModel<?> biped) {
            root.getChild(PartNames.HEAD).loadPose(biped.head.storePose());
            root.getChild(PartNames.HAT).loadPose(biped.hat.storePose());
        }
    }
}
