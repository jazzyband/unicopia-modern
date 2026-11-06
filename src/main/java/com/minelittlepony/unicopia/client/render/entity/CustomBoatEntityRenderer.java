package com.minelittlepony.unicopia.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public class CustomBoatEntityRenderer<T extends AbstractBoat> extends AbstractBoatRenderer {
    private final Model waterMaskModel;
    private final EntityModel<BoatRenderState> model;

    public CustomBoatEntityRenderer(EntityRendererProvider.Context ctx, EntityType<T> type, boolean chest) {
        super(ctx, EntityType.getKey(type).withPath(path -> "textures/entity/" + (chest ? "chest_boat" : "boat") + "/" + path.replace(chest ? "_chest_boat" : "_boat", "") + ".png"));
        waterMaskModel = new Model.Simple(ctx.bakeLayer(ModelLayers.BOAT_WATER_PATCH), id -> RenderTypes.waterMask());
        model = new BoatModel((chest ? BoatModel.createChestBoatModel() : BoatModel.createBoatModel()).bakeRoot());
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return model;
    }

    @Override
    protected void submitTypeAdditions(BoatRenderState state, PoseStack matrices, SubmitNodeCollector vertexConsumers, int light) {
        if (!state.isUnderWater) {
            vertexConsumers.submitCustomGeometry(matrices, waterMaskModel.renderType(texture), (pose, buffer) -> {
                PoseStack stack = new PoseStack();
                stack.mulPose(pose.pose());
                waterMaskModel.renderToBuffer(stack, buffer, light, OverlayTexture.NO_OVERLAY, -1);
            });
        }
    }
}
