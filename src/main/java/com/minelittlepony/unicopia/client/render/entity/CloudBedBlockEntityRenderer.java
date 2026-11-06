package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.FancyBedBlock.SheetPattern;
import com.minelittlepony.unicopia.block.cloud.CloudBedBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

public class CloudBedBlockEntityRenderer implements BlockEntityRenderer<CloudBedBlock.Tile, CloudBedBlockEntityRenderer.State> {
    private final ModelPart bedHead = getHeadTexturedModelData().bakeRoot();
    private final ModelPart bedFoot = getFootTexturedModelData().bakeRoot();
    private final ModelPart bedSheetsHead = getSheetsTexturedModelData(0, 3, 13).bakeRoot();
    private final ModelPart bedSheetsFoot = getSheetsTexturedModelData(22, 0, 15).bakeRoot();

    public CloudBedBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    public CloudBedBlockEntityRenderer() {
    }

    public static LayerDefinition getSheetsTexturedModelData(int v, int y, int height) {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        root.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, v).addBox(0, y, 0, 16, height, 6), PartPose.ZERO);
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition getHeadTexturedModelData() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        root.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(0, 3, 0, 16, 13, 6), PartPose.ZERO);
        root.addOrReplaceChild("head_board", CubeListBuilder.create()
                .texOffs(52, 24).addBox(7, -4, -3, 2, 13, 3)
                .texOffs(0, 43).addBox(-6, -7, -2, 13, 15, 3)
                .texOffs(52, 24).addBox(-8, -4, -3, 2, 13, 3), PartPose.offsetAndRotation(7.5F, 1, 0, Mth.HALF_PI, 0, 0));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition getFootTexturedModelData() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        root.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 22).addBox(0, 0, 0, 16, 15, 6), PartPose.ZERO);
        root.addOrReplaceChild("foot_board", CubeListBuilder.create()
                .texOffs(52, 40).addBox(6, -1, -2, 3, 10, 3)
                .texOffs(0, 43).addBox(-6, -3, -3, 13, 11, 3)
                .texOffs(52, 40).addBox(-8, -1, -2, 3, 10, 3), PartPose.offsetAndRotation(7.5F, 15, 0, Mth.HALF_PI, 0, 0));
        return LayerDefinition.create(data, 64, 64);
    }

    public static class State extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        public String base;
        public SheetPattern pattern;
        public Direction facing;
        public BedPart part;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CloudBedBlock.Tile entity, State state, float tickDelta, Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tickDelta, cameraPos, breakProgress);
        BlockState blockState = entity.getBlockState();
        state.base = entity.getBase();
        state.pattern = entity.getPattern();
        state.facing = blockState.hasProperty(BedBlock.FACING) ? blockState.getValue(BedBlock.FACING) : Direction.SOUTH;
        state.part = blockState.hasProperty(BedBlock.PART) ? blockState.getValue(BedBlock.PART) : BedPart.HEAD;
    }

    @Override
    public void submit(State state, PoseStack matrices, net.minecraft.client.renderer.SubmitNodeCollector vertices,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        boolean head = state.part == BedPart.HEAD;
        renderModel(matrices, vertices, head ? bedHead : bedFoot, state.facing,
                baseType(state.base), state.lightCoords, 0, false, false);
        if (state.pattern != SheetPattern.NONE && state.pattern != SheetPattern.WHITE) {
            renderModel(matrices, vertices, head ? bedSheetsHead : bedSheetsFoot, state.facing,
                    sheetsType(state.pattern), state.lightCoords, 0, false, true);
        }
    }

    public void renderAsItem(SheetPattern pattern, Identifier texture, boolean translucent, PoseStack matrices,
            net.minecraft.client.renderer.SubmitNodeCollector vertices, int light, int overlay) {
        net.minecraft.client.renderer.rendertype.RenderType base = translucent
                ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
        renderModel(matrices, vertices, bedHead, Direction.SOUTH, base, light, overlay, false, false);
        renderModel(matrices, vertices, bedFoot, Direction.SOUTH, base, light, overlay, true, false);
        if (pattern != SheetPattern.NONE) {
            renderModel(matrices, vertices, bedSheetsHead, Direction.SOUTH, sheetsType(pattern), light, overlay, false, true);
            renderModel(matrices, vertices, bedSheetsFoot, Direction.SOUTH, sheetsType(pattern), light, overlay, true, true);
        }
    }

    private net.minecraft.client.renderer.rendertype.RenderType baseType(String base) {
        Identifier texture = Unicopia.id("textures/entity/bed/" + base + ".png");
        return base.equalsIgnoreCase("cloud") ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
    }

    private net.minecraft.client.renderer.rendertype.RenderType sheetsType(SheetPattern pattern) {
        return RenderTypes.entityCutout(Unicopia.id("textures/entity/bed/sheets/" + pattern.getSerializedName() + ".png"));
    }

    private void renderModel(PoseStack matrices, net.minecraft.client.renderer.SubmitNodeCollector vertices, ModelPart part,
            Direction direction, net.minecraft.client.renderer.rendertype.RenderType layer,
            int light, int overlay, boolean translate, boolean sheets) {
        matrices.pushPose();
        matrices.translate(0, 0.5625f, translate ? -1 : 0);
        matrices.rotate(Axis.XP.rotationDegrees(90));
        matrices.translate(0.5f, 0.5f, 0.5f);
        if (sheets) matrices.scale(1.002F, 1.002F, 1.002F);
        matrices.rotate(Axis.ZP.rotationDegrees(180 + direction.toYRot()));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        vertices.submitCustomGeometry(matrices, layer, (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            part.render(stack, buffer, light, overlay);
        });
        matrices.popPose();
    }
}
