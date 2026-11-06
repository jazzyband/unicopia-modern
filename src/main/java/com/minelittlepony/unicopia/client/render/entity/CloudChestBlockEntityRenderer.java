package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;

public class CloudChestBlockEntityRenderer extends ChestRenderer<ChestBlockEntity> {
    private static final BrightnessCombiner<ChestBlockEntity> LIGHTING = new BrightnessCombiner<>();
    private final Model[] models;

    public CloudChestBlockEntityRenderer(Context ctx) {
        super(ctx);
        models = new Model[] {
                new Model(Model.getSingleChestModelData().bakeRoot(), Unicopia.id("textures/entity/chest/cloud.png")),
                new Model(Model.getLeftChestModelData().bakeRoot(), Unicopia.id("textures/entity/chest/cloud_left.png")),
                new Model(Model.getRightChestModelData().bakeRoot(), Unicopia.id("textures/entity/chest/cloud_right.png"))
        };
    }

    @Override
    public void submit(ChestRenderState state, PoseStack matrices, SubmitNodeCollector vertices, CameraRenderState camera) {
        Model model = models[state.type.ordinal()];
        matrices.pushPose();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.rotate(Axis.YP.rotationDegrees(-state.facing.toYRot()));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        model.setAngles(state.open);
        vertices.submitCustomGeometry(matrices, RenderTypes.entityTranslucent(model.texture), (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            model.render(stack, buffer, state.lightCoords, 0);
        });
        matrices.popPose();
    }

    public static DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> getProperties(BlockState state, ChestBlockEntity entity) {
        return entity.getLevel() != null
                ? ((AbstractChestBlock<?>)state.getBlock()).combine(state, entity.getLevel(), entity.getBlockPos(), true)
                : DoubleBlockCombiner.Combiner::acceptNone;
    }

    public static class Model {
        private final ModelPart tree;
        private final ModelPart lid;

        private final Identifier texture;

        public Model(ModelPart tree, Identifier texture) {
            this.tree = tree;
            this.lid = tree.getChild("lid");
            this.texture = texture;
        }

        public static LayerDefinition getSingleChestModelData() {
            MeshDefinition data = new MeshDefinition();
            PartDefinition root = data.getRoot();
            root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 19).addBox(1, 0, 1, 14, 10, 14, CubeDeformation.NONE), PartPose.ZERO);
            root.addOrReplaceChild("lid", CubeListBuilder.create()
                    .texOffs(0, 0).addBox(6, -2, 13.8F, 2, 4, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(6, -1, 14, 2, 2, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(0, 0, 0, 14, 5, 14, new CubeDeformation(0.3F)), PartPose.offset(1, 9, 1))
                    .addOrReplaceChild("lock_r1", CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-2, -4, -0.5F, 2, 4, 1, CubeDeformation.NONE), PartPose.offsetAndRotation(5, 1, 14.3F, 0, 0, 1.5708F));
            return LayerDefinition.create(data, 64, 64);
        }

        public static LayerDefinition getLeftChestModelData() {
            MeshDefinition data = new MeshDefinition();
            PartDefinition root = data.getRoot();
            root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 19).addBox(0, 0, 1, 15, 10, 14, CubeDeformation.NONE), PartPose.ZERO);
            root.addOrReplaceChild("lid", CubeListBuilder.create()
                    .texOffs(0, 0).addBox(6, -2, 13.8F, 2, 4, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(6, -1, 14, 2, 2, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(0, 0, 0, 15, 5, 14, new CubeDeformation(0.3F)), PartPose.offset(0, 9, 1))
                    .addOrReplaceChild("lock_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-2, -4, -0.5F, 2, 4, 1, CubeDeformation.NONE), PartPose.offsetAndRotation(5, 1, 14.3F, 0, 0, 1.5708F));
            return LayerDefinition.create(data, 64, 64);
        }

        public static LayerDefinition getRightChestModelData() {
            MeshDefinition data = new MeshDefinition();
            PartDefinition root = data.getRoot();
            root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 19).addBox(1, 0, 1, 15, 10, 14, CubeDeformation.NONE), PartPose.ZERO);
            root.addOrReplaceChild("lid", CubeListBuilder.create()
                    .texOffs(0, 0).addBox(7, -2, 13.8F, 2, 4, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(7, -1, 14, 2, 2, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(0, 0, 0, 15, 5, 14, new CubeDeformation(0.3F)), PartPose.offset(1, 9, 1))
                    .addOrReplaceChild("lock_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-2, -4, -0.5F, 2, 4, 1, CubeDeformation.NONE), PartPose.offsetAndRotation(6, 1, 14.3F, 0, 0, 1.5708F));
            return LayerDefinition.create(data, 64, 64);
        }

        public void setAngles(float openness) {
            lid.xRot = -(openness * 1.5707964f);
        }

        public void render(PoseStack matrices, VertexConsumer vertices, int light, int overlay) {
            tree.render(matrices, vertices, light, overlay);
        }
    }
}
