package com.minelittlepony.unicopia.client.render.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class SombraEntityModel extends EntityModel<SombraEntityRenderer.State> {
	private final ModelPart head;
	private final ModelPart upperJaw;
	private final ModelPart lowerJaw;

	private final ModelPart body;

	public SombraEntityModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.upperJaw = head.getChild("upper_jaw");
		this.lowerJaw = head.getChild("lower_jaw");
		this.body = root.getChild("body");
	}

	public static LayerDefinition getTexturedModelData() {
		MeshDefinition data = new MeshDefinition();
		PartDefinition root = data.getRoot();

		PartDefinition neck = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(0, 20, 0, 0, 0, 0.0436F));
		neck.addOrReplaceChild("tail", CubeListBuilder.create()
		        .texOffs(50, 12).addBox(3.0958F, -3.8679F, -1, 3, 7, 3, CubeDeformation.NONE), PartPose.offsetAndRotation(4, 3, -1, 0, 0, -1.0036F));
		neck.addOrReplaceChild("neck", CubeListBuilder.create()
		        .texOffs(32, 10).addBox(2.0438F, -5.9668F, -3, 4, 7, 5, CubeDeformation.NONE), PartPose.offsetAndRotation(0, 0, 0, 0, 0, -0.6981F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
		        .texOffs(0, 0).addBox(-5, -13, -4, 8, 8, 8, CubeDeformation.NONE) // head
        		.texOffs(0, 4).addBox(1, -15, -4, 2, 2, 2, CubeDeformation.NONE)  // ear
        		.texOffs(0, 4).addBox(1, -15, 2, 2, 2, 2, CubeDeformation.NONE)   // ear
        		, PartPose.offset(0, 20, 0));

		head.addOrReplaceChild("mane", CubeListBuilder.create()
		        .texOffs(32, 0).addBox(-2.4982F, -6.1228F, -1.5F, 8, 7, 3, CubeDeformation.NONE), PartPose.offsetAndRotation(-1, -12, 0, 0, 0, 0.48F));

		head.addOrReplaceChild("lower_jaw", CubeListBuilder.create()
		        .texOffs(32, 22).addBox(-5, 0, -3, 7, 2, 6, CubeDeformation.NONE), PartPose.offsetAndRotation(-1, -3, 0, 0, 0, -0.5672F));
		head.addOrReplaceChild("upper_jaw", CubeListBuilder.create()
		        .texOffs(32, 30).addBox(-5, -2, -3, 6, 2, 6, CubeDeformation.NONE), PartPose.offsetAndRotation(-4, -5, 0, 0, 0, -0.1745F));
		head.addOrReplaceChild("hair", CubeListBuilder.create()
		        .texOffs(0, 16).addBox(-2, -8, -4, 8, 9, 8, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-3, -7.75F, 0, 0, 0, 0.0436F));

		PartDefinition crown = head.addOrReplaceChild("crown", CubeListBuilder.create()
		        .texOffs(0, 16).addBox(-2, -8, -4, 8, 3, 8, new CubeDeformation(0.5F))
		        .texOffs(1, 1).addBox(-3, -6, -0.75F, 1, 1, 1, CubeDeformation.NONE), PartPose.offsetAndRotation(-4, -8.75F, 0, 0, 0, 0.1309F));

		crown.addOrReplaceChild("spike_r2", CubeListBuilder.create()
		        .texOffs(2, 20).addBox(-1, -4, 0, 1, 4, 0, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -5, 4.25F, -0.5087F, -0.1298F, -0.228F));
		crown.addOrReplaceChild("spike_l2", CubeListBuilder.create()
		        .texOffs(2, 20).addBox(-1, -4, 0, 1, 4, 0, CubeDeformation.NONE), PartPose.offsetAndRotation(3, -5, -4.25F, 0.5236F, 0, 0));
		crown.addOrReplaceChild("spike_l1", CubeListBuilder.create()
		        .texOffs(2, 20).addBox(-1, -4, 0, 1, 4, 0, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -5, -4.25F, 0.5087F, -0.1298F, -0.228F));
		crown.addOrReplaceChild("spike_r1", CubeListBuilder.create()
		        .texOffs(2, 20).addBox(-1, -4, 0, 1, 4, 0, CubeDeformation.NONE), PartPose.offsetAndRotation(3, -5, 4.25F, -0.5236F, 0, 0));

		head.addOrReplaceChild("horn", CubeListBuilder.create()
		        .texOffs(0, 0).addBox(2.2139F, -6.8302F, -1, 1, 3, 1, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-3, -6, 0.5F, 0, 0, -0.829F))
	            .addOrReplaceChild("bone", CubeListBuilder.create()
    		        .texOffs(4, 0).addBox(2.5F, -6.3301F, -0.5F, 1, 2, 1, CubeDeformation.NONE), PartPose.offsetAndRotation(-1, -3, -0.5F, 0, 0, 0.1745F))
    		        .addOrReplaceChild("horn_tip", CubeListBuilder.create()
		                .texOffs(4, 0).addBox(1.5035F, -6.7686F, -0.5F, 1, 2, 1, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0, -2, 0, 0, 0, 0.2182F));

		return LayerDefinition.create(data, 64, 64);
	}

	public void setAngles(SombraEntityRenderer.State state) {
	    super.setupAnim(state);
	    ModelPart part = root();
        part.y = state.scale * -20;
        part.xScale = state.scale;
        part.yScale = state.scale;
        part.zScale = state.scale;

        lowerJaw.resetPose();
        lowerJaw.y -= state.jawsOpenAmount * 3;
        lowerJaw.x -= state.jawsOpenAmount * 3;
        lowerJaw.zRot += state.jawsOpenAmount - 0.9F;

        upperJaw.resetPose();
        upperJaw.zRot -= state.jawsOpenAmount * 0.2F;

	    part.yRot = -Mth.HALF_PI;
	    part.y += Mth.sin(state.ageInTicks * 0.05F);
	    part.z = Mth.cos(state.ageInTicks * 0.045F);

	    head.xRot = state.xRot * Mth.DEG_TO_RAD;
	    head.yRot = state.yRot * Mth.DEG_TO_RAD;

	    body.zRot = state.walkAnimationSpeed * 0.3F;
	}
}
