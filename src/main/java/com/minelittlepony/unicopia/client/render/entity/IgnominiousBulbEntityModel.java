package com.minelittlepony.unicopia.client.render.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

public class IgnominiousBulbEntityModel extends EntityModel<IgnominiousBulbEntityRenderer.State> {

    private final ModelPart head;
    private final ModelPart leaves;

    public IgnominiousBulbEntityModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        head = root.getChild("head");
        leaves = root.getChild("leaves");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-24.0432F, -0.9905F, -48.1305F, 48, 23, 48, CubeDeformation.NONE), PartPose.offsetAndRotation(0, 7, 24, -0.1308F, 0.0057F, 0.0433F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(112, 0).addBox(-16, -27, 5, 32, 0, 32, CubeDeformation.NONE), PartPose.offsetAndRotation(1.3731F, -21.6152F, -11.61F, -1.9802F, 0.1003F, 0.0006F));
        head.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 71).addBox(-23, -16, -46, 46, 23, 46, CubeDeformation.NONE), PartPose.offsetAndRotation(-0.0432F, -0.9905F, -0.1305F, -0.1309F, 0, 0));

        PartDefinition leaves = root.addOrReplaceChild("leaves", CubeListBuilder.create(), PartPose.offset(0, 27, -1));
        leaves.addOrReplaceChild("leaf_1", CubeListBuilder.create().texOffs(112, 0).addBox(-16, 3, -55, 32, 0, 32, CubeDeformation.NONE), PartPose.rotation(2.8316F, -1.0127F, -3.0858F));
        leaves.addOrReplaceChild("leaf_2", CubeListBuilder.create().texOffs(112, 0).addBox(-14, 12, -60, 32, 0, 32, CubeDeformation.NONE), PartPose.rotation(2.6063F, -0.075F, -3.1196F));
        leaves.addOrReplaceChild("leaf_3", CubeListBuilder.create().texOffs(112, 0).addBox(-20, 1, -51, 32, 0, 32, CubeDeformation.NONE), PartPose.rotation(-0.7079F, -1.4622F, 0.4842F));
        leaves.addOrReplaceChild("leaf_4", CubeListBuilder.create().texOffs(112, 0).addBox(-16, 6, -58, 32, 0, 32, CubeDeformation.NONE), PartPose.rotation(-0.4967F, 0.1003F, 0.0006F));
        leaves.addOrReplaceChild("leaf_5", CubeListBuilder.create().texOffs(-64, 140).addBox(-30, 7, -83, 64, 0, 64, CubeDeformation.NONE), PartPose.rotation(2.8708F, -0.1388F, -3.0651F));
        leaves.addOrReplaceChild("leaf_6", CubeListBuilder.create().texOffs(-64, 140).addBox(-30, 7, -83, 64, 0, 64, CubeDeformation.NONE), PartPose.rotation(-1.084F, -1.4961F, 0.807F));
        leaves.addOrReplaceChild("leaf_7", CubeListBuilder.create().texOffs(-64, 140).addBox(-30, 3, -83, 64, 0, 64, CubeDeformation.NONE), PartPose.rotation(-0.2651F, -0.035F, -0.0332F));
        leaves.addOrReplaceChild("leaf_8", CubeListBuilder.create().texOffs(-64, 140).addBox(-30, 7, -83, 64, 0, 64, CubeDeformation.NONE), PartPose.rotation(-1.95F, 1.4932F, -1.6857F));
        leaves.addOrReplaceChild("leaf_9", CubeListBuilder.create().texOffs(112, 0).addBox(-16, 10, -57, 32, 0, 32, CubeDeformation.NONE), PartPose.rotation(-2.5197F, 1.466F, -2.0202F));
        leaves.addOrReplaceChild("leaf_10", CubeListBuilder.create().texOffs(112, 0).addBox(-8, 3, -61, 32, 0, 32, CubeDeformation.NONE), PartPose.rotation(2.9628F, 0.6133F, -2.978F));
        leaves.addOrReplaceChild("leaf_11", CubeListBuilder.create().texOffs(112, 0).addBox(-13, 6, -60, 32, 0, 32, CubeDeformation.NONE), PartPose.rotation(-0.4768F, 0.8786F, -0.1186F));
        leaves.addOrReplaceChild("leaf_12", CubeListBuilder.create().texOffs(112, 0).addBox(-16, 0, -59, 32, 0, 32, CubeDeformation.NONE), PartPose.rotation(-0.2911F, -0.5857F, 0.0605F));

        return LayerDefinition.create(data, 256, 256);
    }

    public void setupAnim(IgnominiousBulbEntityRenderer.State state) {
        super.setupAnim(state);
        ModelPart part = root();
        part.xScale = state.scale;
        part.yScale = state.scale;
        part.zScale = state.scale;
        part.y = (1 - state.scale) * 24;
        part.yRot = state.yaw;

        head.yScale = 1 - Mth.sin(state.ageInTicks * 0.05F) * 0.02F;

        float hScale = 1 + Mth.cos(state.ageInTicks * 0.06F) * 0.02F;
        head.xScale = hScale;
        head.zScale = hScale;

        head.xRot = Mth.sin(state.ageInTicks * 0.02F) * 0.02F;
        head.yRot = (Mth.cos(state.ageInTicks * 0.02F) * 0.02F);

        leaves.yScale = 1 + Mth.sin(state.ageInTicks * 0.05F) * 0.12F;
        leaves.yRot = -part.yRot;
    }
}
