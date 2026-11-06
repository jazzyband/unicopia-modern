package com.minelittlepony.unicopia.client.render.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class TentacleEntityModel extends EntityModel<TentacleEntityRenderer.State> {
	private final Map<String, ModelPart> parts = new HashMap<>();
	private final List<ModelPart> bones = new ArrayList<>();
	private final List<ModelPart> brushes;

	private final ModelPart tip;

	public TentacleEntityModel(ModelPart root) {
	    super(root, RenderTypes::entityTranslucent);
		for (String key : List.of("bone_a", "bone_b", "bone_c", "bone_d", "bone_e", "bone_f", "bone_g", "bone_h")) {
    		parts.put(key, root = root.getChild(key));
    		bones.add(root);
		}
		var bone_a = parts.get("bone_a");
		brushes = List.of(
		        bone_a.getChild("brush_1"),
		        bone_a.getChild("brush_2"),
		        bone_a.getChild("brush_3"),
		        bone_a.getChild("brush_4"),
		        parts.get("bone_c").getChild("flower_1"),
		        parts.get("bone_d").getChild("flower_2"),
		        parts.get("bone_f").getChild("flower_3"));
		tip = parts.get("bone_h").getChild("tip");
	}

	public static LayerDefinition getTexturedModelData() {
	    MeshDefinition data = new MeshDefinition();
        data.getRoot()
                .addOrReplaceChild("bone_a", CubeListBuilder.create().texOffs(0, 0).addBox(-7, -10, -7, 14, 16, 14, CubeDeformation.NONE), PartPose.offset(0, 24, 0))
                .addOrReplaceChild("bone_b", CubeListBuilder.create().texOffs(0, 30).addBox(-6, -18, -6, 12, 19, 12, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -9, 0, -0.2618F, 0, 0))
                .addOrReplaceChild("bone_c", CubeListBuilder.create().texOffs(48, 20).addBox(-5, -23, -5, 10, 23, 10, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -16, 0, 0, 0, 0))
                .addOrReplaceChild("bone_d", CubeListBuilder.create().texOffs(40, 53).addBox(-4, -23, -4, 8, 21, 8, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -18, 0, -0.1745F, 0, 0))
                .addOrReplaceChild("bone_e", CubeListBuilder.create().texOffs(0, 61).addBox(-3, -25, -3, 6, 22, 6, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -18, 0, 0.1745F, 0, 0))
                .addOrReplaceChild("bone_f", CubeListBuilder.create().texOffs(72, 53).addBox(-3, -17, -3, 6, 15, 6, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -22, 0, 0.1745F, 0, 0))
                .addOrReplaceChild("bone_g", CubeListBuilder.create().texOffs(56, 0).addBox(-2.6F, -16, -2.6F, 5.2F, 15, 5.2F, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -15, 0, 0.3054F, 0, 0))
                .addOrReplaceChild("bone_h", CubeListBuilder.create().texOffs(24, 61).addBox(-2.1F, -17, -2.1F, 4.2F, 15, 4.2F, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -13, 0, 0.2618F, 0, 0));
        return LayerDefinition.create(addFlowers(data), 128, 128);
	}

	private static MeshDefinition addFlowers(MeshDefinition data) {
        PartDefinition bone_a = data.getRoot().getChild("bone_a");
        createFlowerSinglet(createFlowerSinglet(bone_a.addOrReplaceChild("brush_1", CubeListBuilder.create(), PartPose.offsetAndRotation(-3, 0.6703F, -7.7725F,  0.6103F, -0.0535F, -0.5864F))).addOrReplaceChild("bundle", CubeListBuilder.create(), PartPose.rotation(0.6103F, -0.0535F, -0.5864F)));
        createFlowerSinglet(createFlowerSinglet(bone_a.addOrReplaceChild("brush_2", CubeListBuilder.create(), PartPose.offsetAndRotation(-5, 0.6703F,  4.2275F, -1.2698F,  0.9678F, -1.7981F))).addOrReplaceChild("bundle", CubeListBuilder.create(), PartPose.rotation(-1.2698F, 0.9678F, -1.7981F)));
        createFlowerSinglet(createFlowerSinglet(bone_a.addOrReplaceChild("brush_3", CubeListBuilder.create(), PartPose.offsetAndRotation( 6, 0.6703F,  4.2275F, -2.4079F,  0.2344F, -2.5374F))).addOrReplaceChild("bundle", CubeListBuilder.create(), PartPose.rotation(-2.4079F, 0.2344F, -2.5374F)));
        createFlowerSinglet(createFlowerSinglet(bone_a.addOrReplaceChild("brush_4", CubeListBuilder.create(), PartPose.offsetAndRotation( 6, 0.6703F, -6.7725F,  1.5929F, -0.909F,  -1.1179F))).addOrReplaceChild("bundle", CubeListBuilder.create(), PartPose.rotation(2.9259F, -0.8201F, -2.1974F)));

        PartDefinition bone_c = bone_a.getChild("bone_b").getChild("bone_c");
        createFlowerSinglet(bone_c.addOrReplaceChild("flower_1", CubeListBuilder.create(), PartPose.offsetAndRotation( 4, -15.6242F, -3.3435F,  2.9259F, -0.8201F, -2.4592F)));

        PartDefinition bone_d = bone_c.getChild("bone_d");
        createFlowerSinglet(bone_d.addOrReplaceChild("flower_2", CubeListBuilder.create(), PartPose.offsetAndRotation(-2, -17.1355F,  3.0055F, -2.8606F, -0.5942F,  2.4482F)));
        PartDefinition bone_f = bone_d.getChild("bone_e").getChild("bone_f");
        createFlowerSinglet(bone_f.addOrReplaceChild("flower_3", CubeListBuilder.create(), PartPose.offsetAndRotation(-2, -6.0251F,   0.478F,   2.7587F,  0.3479F,  2.5432F)));

        PartDefinition flower_12 = createFlowerBunch(bone_f.getChild("bone_g").getChild("bone_h").addOrReplaceChild("tip", CubeListBuilder.create(), PartPose.offsetAndRotation(0, -15.1462F, 0.6365F, 2.7587F, 0.3479F, 2.9795F)));
        createFlowerBunch(flower_12.addOrReplaceChild("bundle_1", CubeListBuilder.create(), PartPose.rotation( 2.7587F,  0.3479F, 2.5868F)));
        createFlowerBunch(flower_12.addOrReplaceChild("bundle_2", CubeListBuilder.create(), PartPose.rotation(-1.9412F, -1.0444F, 1.7406F)));
        return data;
	}

	private static PartDefinition createFlowerSinglet(PartDefinition parent) {
        parent.addOrReplaceChild("flower_1", CubeListBuilder.create().texOffs(86, 0).addBox(-14, 0, 0, 14, 0, 14, CubeDeformation.NONE), PartPose.rotation(1.5708F, 0, 0.7854F));
        parent.addOrReplaceChild("flower_2", CubeListBuilder.create().texOffs(86, 0).addBox(-14, 0, 0, 14, 0, 14, CubeDeformation.NONE), PartPose.rotation(3.1416F, 0.7854F, 1.5708F));
        return parent;
	}

	private static PartDefinition createFlowerBunch(PartDefinition parent) {
	    parent.addOrReplaceChild("flower_1", CubeListBuilder.create().texOffs(86, 0).addBox(-50.2096F, 2.6718F, -13.6647F, 14, 0, 14, CubeDeformation.NONE), PartPose.offsetAndRotation(36.0432F, 15.5243F, -3.1436F, 1.5708F, 0, 0.7854F));
	    parent.addOrReplaceChild("flower_2", CubeListBuilder.create().texOffs(86, 0).addBox(-27.1617F, -35.2665F, 9.3832F, 14, 0, 14, CubeDeformation.NONE), PartPose.offsetAndRotation(36.0432F, 15.5243F, -3.1436F, 3.1416F, 0.7854F, 1.5708F));
	    return parent;
	}

	public void setupAnim(TentacleEntityRenderer.State state) {
	    super.setupAnim(state);
	    float age = state.animationFrame;
	    float idleWaveTimer = state.animationTime;

	    float attackCurve = state.attackProgress * -0.5F;
	    float sweepDirection = 1;

	    float bendIntensity = state.bendIntensity;

        root().yRot = (state.yaw * Mth.DEG_TO_RAD) + Mth.HALF_PI * state.attackProgress;

	    for (ModelPart bone : bones) {
	        float idlePitch = Mth.sin(idleWaveTimer) * 0.0226F * bendIntensity;
	        float idleYaw = Mth.cos(idleWaveTimer + 0.53F) * 0.07F;
	        float idleRoll = Mth.sin(idleWaveTimer * 0.2F) * 0.0226F * bendIntensity;
	        idleWaveTimer += 1.5F;
	        bendIntensity += 3F;
	        bone.resetPose();

	        if (!state.growing) {
    	        bone.xRot = Mth.lerp(state.attackProgress, idlePitch, bone.xRot + attackCurve);
    	        bone.yRot = Mth.lerp(state.attackProgress, idleYaw, bone.yRot + sweepDirection * attackCurve);
    	        bone.zRot = Mth.lerp(state.attackProgress, idleRoll, bone.zRot);
	        }
	        attackCurve *= 1.04F;
	    }

	    float direction = 1;

	    for (ModelPart brush : brushes) {
	        brush.resetPose();
	        brush.xRot += Mth.sin(age * 0.003F) * 0.03F;
	        brush.yRot += Mth.cos(age * 0.003F) * 0.1F * (direction *= -1);
	    }

	    tip.resetPose();
	    tip.xRot += Mth.sin(age * 0.003F) * 0.3F;
	    tip.yRot += Mth.sin(age * 0.03F) * 0.3F;
	}
}
