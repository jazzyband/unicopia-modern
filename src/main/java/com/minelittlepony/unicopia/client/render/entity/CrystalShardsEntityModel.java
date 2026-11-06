package com.minelittlepony.unicopia.client.render.entity;

import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class CrystalShardsEntityModel extends EntityModel<CrystalShardsEntityRenderer.State> {
	private final List<ModelPart> crystals;

	public CrystalShardsEntityModel(ModelPart root) {
		super(root);
		this.crystals = List.of(root.getChild("west"), root.getChild("north"), root.getChild("south"), root.getChild("east"), root.getChild("primary"));
	}

	public static LayerDefinition getTexturedModelData() {
		MeshDefinition data = new MeshDefinition();
		PartDefinition root = data.getRoot();
		root.addOrReplaceChild("west", CubeListBuilder.create().texOffs(12, 0).addBox(-1, -10, -2, 3, 10, 3, CubeDeformation.NONE), PartPose.rotation(0.4075F, 0.0378F, 1.0216F));
		root.addOrReplaceChild("north", CubeListBuilder.create().texOffs(12, 0).addBox(-1, -10, -2, 3, 10, 3, CubeDeformation.NONE), PartPose.rotation(1.817F, 0.6399F, 2.0582F));
		root.addOrReplaceChild("south", CubeListBuilder.create().texOffs(12, 0).addBox(-2, -9, -2, 3, 10, 3, CubeDeformation.NONE), PartPose.rotation(-1.8254F, 0.6819F, -1.3949F));
		root.addOrReplaceChild("east", CubeListBuilder.create().texOffs(12, 13).addBox(-2, -8, -2, 3, 8, 3, CubeDeformation.NONE), PartPose.rotation(-0.1151F, 0.3935F, -0.7545F));
		root.addOrReplaceChild("primary", CubeListBuilder.create().texOffs(0, 0).addBox(-2, -20, -2, 3, 20, 3, CubeDeformation.NONE), PartPose.rotation(0.2411F, 0.3339F, 0.1819F));
		return LayerDefinition.create(data, 32, 32);
	}

	public void setupAnim(CrystalShardsEntityRenderer.State state) {
	    super.setupAnim(state);
	    float offset = 0;
	    float amplitude = 0.02F;

	    for (ModelPart part : crystals) {
	        part.resetPose();

	        if (state.shaking) {
	            float animationTime = (state.ageInTicks + ++offset) * 122F;
	            float sin = Mth.sin(animationTime) * amplitude;

	            part.xRot += sin;
	            part.yRot += Mth.cos(animationTime) * amplitude;
	            part.zRot += -sin;
	        }
	    }
	}
}
