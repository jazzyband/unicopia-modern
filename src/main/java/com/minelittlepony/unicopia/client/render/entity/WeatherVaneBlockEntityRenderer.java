package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.WeatherVaneBlock;
import com.minelittlepony.unicopia.block.WeatherVaneBlock.WeatherVane;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class WeatherVaneBlockEntityRenderer implements BlockEntityRenderer<WeatherVaneBlock.WeatherVane, WeatherVaneBlockEntityRenderer.State> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/weather_vane.png");

    private final ModelPart root;
    private final ModelPart pole;

    public WeatherVaneBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        root = getTexturedModelData().bakeRoot();
        pole = root.getChild("pole");
    }

    private static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        root.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(30, 14).mirror().addBox(-9, -1, 7, 2, 1, 2, CubeDeformation.NONE), PartPose.offset(8, 0, -8));

        PartDefinition pole = root.addOrReplaceChild("pole", CubeListBuilder.create(), PartPose.ZERO);

        pole.addOrReplaceChild("ew_arrow", CubeListBuilder.create()
                .texOffs(0, -16).addBox(0, -12, -8, 0, 5, 16, CubeDeformation.NONE), PartPose.rotation(0, 0.7854F, 0));
        pole.addOrReplaceChild("apple", CubeListBuilder.create()
                .texOffs(0, 2).addBox(0, -27, -7, 0, 15, 15, CubeDeformation.NONE)
                .texOffs(0, -11).addBox(0, -9, -8, 0, 5, 16, CubeDeformation.NONE)
                .texOffs(32, 0).addBox(-0.5F, -14, -0.5F, 1, 14, 1, CubeDeformation.NONE), PartPose.ZERO);


        return LayerDefinition.create(modelData, 64, 32);
    }


    public static class State extends BlockEntityRenderState {
        public float angle;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WeatherVane entity, State state, float tickDelta, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tickDelta, cameraPos, breakProgress);
        state.angle = entity.getAngle(tickDelta);
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector vertices, CameraRenderState camera) {
        matrices.pushPose();
        matrices.scale(1, -1, -1);
        matrices.translate(0.5F, 0, -0.5F);

        vertices.submitCustomGeometry(matrices, RenderTypes.entityTranslucent(TEXTURE), (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            pole.yRot = state.angle;
            root.render(stack, buffer, state.lightCoords, 0);
        });

        matrices.popPose();
    }
}
