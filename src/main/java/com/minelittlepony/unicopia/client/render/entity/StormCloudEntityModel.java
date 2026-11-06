package com.minelittlepony.unicopia.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
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
import org.joml.Vector3f;

public class StormCloudEntityModel extends EntityModel<StormCloudEntityRenderer.State> {
	private final ModelPart smallPuffs;
	private final List<ModelPart> smallPuffCubes;

	private final ModelPart anvilHeads;
	private final List<ModelPart> anvilHeadCubes;

	private final ModelPart puff;

	public StormCloudEntityModel(ModelPart root) {
	    super(root, RenderTypes::entityTranslucent);
		this.smallPuffs = root.getChild("small_puffs");
		this.smallPuffCubes = smallPuffs.getAllParts();
		this.anvilHeads = root.getChild("anvil_heads");
		this.anvilHeadCubes = anvilHeads.getAllParts();
		this.puff = root.getChild("puff");
		this.puff.skipDraw = true;
	}

	public static LayerDefinition getTexturedModelData() {
	    MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition small_puffs = root.addOrReplaceChild("small_puffs", CubeListBuilder.create(), PartPose.offset(0, 24, 0));

        small_puffs.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                .texOffs(0, 37).addBox(-2, -20, -20, 7, 17, 20, CubeDeformation.NONE)
                .texOffs(0, 37).addBox(-2, -13, -40, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -6, 0, 0, 0, 1.5708F));

        small_puffs.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 37).addBox(-2, -7, -25, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -6, 0, 0.1745F, 0, 1.5708F));
        small_puffs.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 37).addBox(-7, -7, -7, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(-15, -1, 15, 0.2182F, 0, 1.5708F));
        small_puffs.addOrReplaceChild("cube_r4", CubeListBuilder.create().addBox(-13, -14, -31, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(2, -2, 19, 0, 0, 1.6581F));
        small_puffs.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 37).addBox(-6, -17, -6, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(2, -2, 19, -0.3491F, 0, 1.5708F));
        small_puffs.addOrReplaceChild("cube_r6", CubeListBuilder.create().addBox(-3.5F, -4.5F, -23, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(0.4471F, -12.1925F, 36, 0, 0, -3.1416F));
        small_puffs.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 37).addBox(-6, -17, -12, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(-15, -2, 26, -0.829F, 0, 1.5708F));
        small_puffs.addOrReplaceChild("cube_r8", CubeListBuilder.create()
                .texOffs(0, 37).addBox(-6, -20, -21, 7, 17, 20, CubeDeformation.NONE)
                .addBox(-13, -20, -40, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(-15, -2, 26, 0, 0, 1.5708F));

        small_puffs.addOrReplaceChild("cube_r9", CubeListBuilder.create().addBox(-15, -13, -18, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(-15, -1, 15, 0, 0, 1.4835F));
        small_puffs.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 37).addBox(-7, -3, -25, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(-15, -1, 15, 0, 0, 1.5708F));
        small_puffs.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 37).addBox(-2, -4, -7, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -6, 0, -0.0873F, 0, 1.5708F));
        small_puffs.addOrReplaceChild("cube_r12", CubeListBuilder.create().addBox(-9.5F, -8.5F, -10, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(8.842F, -13.6476F, -20.7083F, 0.1739F, 0.0151F, -0.0423F));
        small_puffs.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 37).addBox(-2, -19, -6, 7, 17, 20, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -6, 0, -0.3043F, 0, 1.5708F));

        PartDefinition anvil_heads = root.addOrReplaceChild("anvil_heads", CubeListBuilder.create(), PartPose.offset(-2, 1, 0));
        anvil_heads.addOrReplaceChild("cube_r14", CubeListBuilder.create().addBox(-3.5F, -1.5F, -4, 7, 17, 20, new CubeDeformation(2)), PartPose.offsetAndRotation(-5.5F, -4.5F, 16, -0.3927F, 0, 0));
        anvil_heads.addOrReplaceChild("cube_r15", CubeListBuilder.create().addBox(-2, -18, 4, 7, 17, 20, new CubeDeformation(5)), PartPose.offsetAndRotation(0, -6, 0, -0.1309F, 0, 0));
        anvil_heads.addOrReplaceChild("cube_r16", CubeListBuilder.create().addBox(0, -1.5F, -12.5F, 7, 17, 20, new CubeDeformation(5)), PartPose.offsetAndRotation(0, -6, 0, 0.3491F, 0, 0));
        anvil_heads.addOrReplaceChild("cube_r17", CubeListBuilder.create().addBox(7, 9, 0, 7, 17, 20, new CubeDeformation(2)), PartPose.offsetAndRotation(0, -6, 0, 0.2618F, 0, 0));
        anvil_heads.addOrReplaceChild("cube_r18", CubeListBuilder.create().addBox(-12, 3, -9, 7, 17, 20, new CubeDeformation(2)), PartPose.offsetAndRotation(0, -6, 0, 0.3927F, 0, 0));

        root.addOrReplaceChild("puff", CubeListBuilder.create()
                .texOffs(0, 77).addBox(-6, -11, -5, 18, 5, 9, CubeDeformation.NONE)
                .texOffs(0, 80).addBox(-8, -10, 4, 14, 4, 6, CubeDeformation.NONE)
                .texOffs(0, 74).addBox(-2, -10, -11, 12, 5, 11, CubeDeformation.NONE)
                .texOffs(7, 77).addBox(-14, -11, -10, 12, 5, 9, CubeDeformation.NONE)
                .texOffs(0, 79).addBox(-2, -13, 0, 8, 3, 6, CubeDeformation.NONE), PartPose.offsetAndRotation(-5, 14, -20, 0, 1.5272F, 0));

        return LayerDefinition.create(data, 64, 128);
	}

	@Override
	public void setupAnim(StormCloudEntityRenderer.State state) {
	    super.setupAnim(state);
	    float dir = 1;
	    float globalScale = 0.012F;

	    float roll = Mth.cos(state.ageInTicks / 10F) * globalScale;
	    float flop = Mth.sin(state.ageInTicks / 30F) * 0.02F * globalScale;

	    for (ModelPart swirl : smallPuffCubes) {
            swirl.resetPose();
            dir = -dir;
            swirl.yRot += roll * 0.01F * dir;
        }

	    for (ModelPart swirl : anvilHeadCubes) {
	        swirl.resetPose();
	        dir = -dir;
	        swirl.xRot += flop + roll * -0.01F * dir;
	        swirl.yRot += roll * 0.01F * dir;
	        swirl.zRot += -flop + roll * 0.01F * dir;
	    }

	    ModelPart part = root();
	    part.y = Mth.sin(state.ageInTicks * 0.25F) * 0.03F * globalScale;
	    part.x = Mth.cos(state.ageInTicks * 0.05125F) * 0.7F * globalScale;
	    part.z = Mth.sin(state.ageInTicks * 0.05125F) * 0.7F * globalScale;
	}

	public void renderPuff(Vector3f position, PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
	    puff.skipDraw = false;
	    puff.resetPose();
        puff.offsetPos(position);
        puff.render(matrices, vertexConsumer, light, overlay, color);
        puff.skipDraw = true;
	}
}
