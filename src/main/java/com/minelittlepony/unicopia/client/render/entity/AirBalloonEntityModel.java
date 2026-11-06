package com.minelittlepony.unicopia.client.render.entity;

import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.client.model.EntityModel;

public class AirBalloonEntityModel extends EntityModel<AirBalloonEntityRenderer.State> {

    private ModelPart main;

    public final boolean isBurner;
    public final boolean isBalloon;
    public final boolean isSandbags;

    private final List<ModelPart> ropes;
    private final List<ModelPart> struts;
    private final List<ModelPart> sandbags;

    public AirBalloonEntityModel(ModelPart root) {
        super(root);
        isBurner = root.hasChild("burner");
        isSandbags = root.hasChild("sandbag_ne");
        isBalloon = root.hasChild("canopy");

        if (isBurner || isBalloon) {
            main = root.getChild(isBalloon ? "canopy" : "burner");
            ropes = List.of(
                    (isBurner ? root : main).getChild("rope_a"), (isBurner ? root : main).getChild("rope_b"),
                    (isBurner ? root : main).getChild("rope_c"), (isBurner ? root : main).getChild("rope_d")
            );
        } else {
            ropes = List.of();
        }
        if (isBurner) {
            struts = List.of(root.getChild("strut_a"), root.getChild("strut_b"));
        } else {
            struts = List.of();
        }

        sandbags = isSandbags ? List.of(
                root.getChild("sandbag_nw"), root.getChild("sandbag_sw"),
                root.getChild("sandbag_ne"), root.getChild("sandbag_se")
        ) : List.of();
    }

    public static LayerDefinition getBasketModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();
        PartDefinition basket = root.addOrReplaceChild("basket", CubeListBuilder.create().texOffs(0, 0).addBox(-16, -1, -16, 32, 2, 30, CubeDeformation.NONE), PartPose.offset(0, 24, 0));
        basket.addOrReplaceChild("walls", CubeListBuilder.create().texOffs(0, 66).addBox(-17, -12, -16, 2, 11, 30, CubeDeformation.NONE)
                .texOffs(64, 68).addBox(15, -12, -16, 2, 11, 30, CubeDeformation.NONE)
                .texOffs(80, 38).addBox(-16, -12, -17, 32, 11, 2, CubeDeformation.NONE)
                .texOffs(0, 32).addBox(8, -12, 13, 8, 11, 2, CubeDeformation.NONE)
                .texOffs(0, 6).addBox(-16, -12, 13, 8, 11, 2, CubeDeformation.NONE), PartPose.ZERO);
        basket.addOrReplaceChild("rim", CubeListBuilder.create().texOffs(40, 34).addBox(-18, -13, -17, 4, 2, 32, CubeDeformation.NONE)
                .texOffs(0, 32).addBox(14, -13, -17, 4, 2, 32, CubeDeformation.NONE)
                .texOffs(80, 32).addBox(-17, -13, -18, 34, 2, 4, CubeDeformation.NONE)
                .texOffs(0, 19).addBox(7, -13, 12, 10, 2, 4, CubeDeformation.NONE)
                .texOffs(0, 0).addBox(-17, -13, 12, 10, 2, 4, CubeDeformation.NONE), PartPose.ZERO);
        return LayerDefinition.create(modelData, 256, 128);
    }

    public static LayerDefinition getBurnerModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();
        root.addOrReplaceChild("burner", CubeListBuilder.create().texOffs(8, 0).addBox(-5.5F, -47, -5.5F, 11, 15, 11, CubeDeformation.NONE), PartPose.offset(0, 24, 0));
        float angle = 0.37854F;
        float half = Mth.HALF_PI;
        root.addOrReplaceChild("rope_d", CubeListBuilder.create().addBox(0, -68, 0, 2, 66, 2, CubeDeformation.NONE), PartPose.offsetAndRotation(-0, -20, -0, angle, 0, -angle));
        root.addOrReplaceChild("rope_c", CubeListBuilder.create().addBox(0, -68, 0, 2, 66, 2, CubeDeformation.NONE), PartPose.offsetAndRotation(-0, -20, -0, -angle, 0, -angle));
        root.addOrReplaceChild("rope_b", CubeListBuilder.create().addBox(0, -68, 0, 2, 66, 2, CubeDeformation.NONE), PartPose.offsetAndRotation( 0, -20,  0, -angle, 0,  angle));
        root.addOrReplaceChild("rope_a", CubeListBuilder.create().addBox(0, -68, 0, 2, 66, 2, CubeDeformation.NONE), PartPose.offsetAndRotation( 0, -20,  0,  angle, 0,  angle));

        root.addOrReplaceChild("strut_a", CubeListBuilder.create()
                .addBox(-27, -40, -30, 2, 40, 2, CubeDeformation.NONE)
                .addBox(-27,  0, -30, 2, 40, 2, CubeDeformation.NONE)
                .addBox( 27, -40, -30, 2, 40, 2, CubeDeformation.NONE)
                .addBox( 27,  0, -30, 2, 40, 2, CubeDeformation.NONE)

                .addBox(-27, -40, 26, 2, 40, 2, CubeDeformation.NONE)
                .addBox(-27,  0, 26, 2, 40, 2, CubeDeformation.NONE)
                .addBox( 27, -40, 26, 2, 40, 2, CubeDeformation.NONE)
                .addBox( 27,  0, 26, 2, 40, 2, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -80, 0, half, 0,  0));
        root.addOrReplaceChild("strut_b", CubeListBuilder.create()
                .addBox(-27, -40, -20, 2, 40, 2, CubeDeformation.NONE)
                .addBox(-27,  0, -20, 2, 40, 2, CubeDeformation.NONE)
                .addBox( 27, -40, -20, 2, 40, 2, CubeDeformation.NONE)
                .addBox( 27,  0, -20, 2, 40, 2, CubeDeformation.NONE)

                .addBox(-27, -40, 30, 2, 40, 2, CubeDeformation.NONE)
                .addBox(-27,  0, 30, 2, 40, 2, CubeDeformation.NONE)
                .addBox( 27, -40, 30, 2, 40, 2, CubeDeformation.NONE)
                .addBox( 27,  0, 30, 2, 40, 2, CubeDeformation.NONE), PartPose.offsetAndRotation(0, -80, 0, half, half, 0));
        return LayerDefinition.create(modelData, 64, 128);
    }

    public static LayerDefinition getCanopyModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();
        PartDefinition balloon = root.addOrReplaceChild("canopy", CubeListBuilder.create().addBox(-54, -178, -59, 112, 120, 112, CubeDeformation.NONE), PartPose.offset(0, 24, 0));
        balloon.addOrReplaceChild("rope_d", CubeListBuilder.create().addBox(-2, -68, 0, 2, 68, 2, CubeDeformation.NONE), PartPose.offsetAndRotation(-14, -11, -16, 0.4363F, 0, -0.4363F));
        balloon.addOrReplaceChild("rope_c", CubeListBuilder.create().addBox(-2, -68, 0, 2, 68, 2, CubeDeformation.NONE), PartPose.offsetAndRotation(-14, -11, 11, -0.4363F, 0, -0.4363F));
        balloon.addOrReplaceChild("rope_b", CubeListBuilder.create().addBox(-2, -68, 0, 2, 68, 2, CubeDeformation.NONE), PartPose.offsetAndRotation(17, -11, 11, -0.4363F, 0, 0.4363F));
        balloon.addOrReplaceChild("rope_a", CubeListBuilder.create().addBox(-2, -68, 0, 2, 68, 2, CubeDeformation.NONE), PartPose.offsetAndRotation(17, -11, -16, 0.4363F, 0, 0.4363F));
        return LayerDefinition.create(modelData, 512, 256);
    }

    public static LayerDefinition getSandbagsModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();
        float offset = 40;
        getHangingBagModelData("sandbag_ne", root, -offset, -offset);
        getHangingBagModelData("sandbag_nw", root, -offset,  offset);
        getHangingBagModelData("sandbag_se", root,  offset, -offset);
        getHangingBagModelData("sandbag_sw", root,  offset,  offset);
        return LayerDefinition.create(modelData, 32, 32);
    }

    public static void getHangingBagModelData(String name, PartDefinition root, float x, float z) {
        PartDefinition bag = root.addOrReplaceChild(name, CubeListBuilder.create()
                .texOffs(16, 19).addBox(-0.5F, 0, -0.5F, 1, 9, 1, CubeDeformation.NONE), PartPose.offset(x, -35, z));
        PartDefinition knot = bag.addOrReplaceChild("knot", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3, 1, -3, 6, 7, 6, CubeDeformation.NONE)
                .texOffs(12, 14).addBox(-2, 0, -2, 4, 1, 4, CubeDeformation.NONE)
                .texOffs(0, 13).addBox(-2, 8, -2, 4, 1, 4, CubeDeformation.NONE), PartPose.offset(0, 9, 0));
        knot.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(8, 14).addBox(0, 8, -2, 0, 4, 4, CubeDeformation.NONE), PartPose.offsetAndRotation(0, 1, 0, 0, -0.7854F, 0));
        knot.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(8, 14).addBox(0, 8, -2, 0, 4, 4, CubeDeformation.NONE), PartPose.offsetAndRotation(0, 1, 0, 0, 0.7854F, 0));
    }

    public void setAngles(AirBalloonEntityRenderer.State state) {
        super.setupAnim(state);
        root.yRot = Mth.PI;

        if (isSandbags) {
            root.yRot -= state.yaw * Mth.DEG_TO_RAD;
        }

        if (isBurner || isBalloon || isSandbags) {
            root.zRot = state.basketRoll;
            root.xRot = state.basketPitch;
        } else {
            root.xRot = 0;
            root.zRot = 0;
        }

        if (isBurner) {
            boolean lifted = state.inflation > 0.8F;
            ropes.forEach(rope -> {
                rope.visible = lifted;
            });
            struts.forEach(strut -> {
                strut.visible = lifted;
            });

            root.x = state.accessoriesPivotX + state.burnerWiggleProgress * Mth.sin(state.ageInTicks) * 2.5F;
            root.z += state.burnerWiggleProgress * Mth.cos(state.ageInTicks) * 2.5F;
            root.y = state.burnerPivoyY + state.burnerWiggleProgress * 7;
        }
        if (isBalloon || isSandbags) {
            root.y = state.burnerWiggleProgress * 3;
            root.x = state.accessoriesPivotX;
            if (state.basket.isOf(WoodType.BAMBOO)) {
                ropes.forEach(rope -> rope.y = 0);
            }
        }

        if (isSandbags) {
            for (int i = 0; i < sandbags.size(); i++) {
                ModelPart bag = sandbags.get(i);
                float pullProgress = state.sandbagPullAmounts[i];
                bag.xRot -= root.xRot * 2.5F * (1 + pullProgress) + state.sandbagsPitch;
                bag.zRot -= root.zRot * 2.5F * (1 + pullProgress) + state.sandbagsRoll;
                if (!state.leashStates.isEmpty()) {
                    bag.zRot *= -1;
                    bag.xRot *= -1;
                }
                float pullAmount = 2 + (2 * pullProgress);
                bag.yScale = pullAmount;
                bag.getChild("knot").yScale = 1/pullAmount;
            }
        }

        for (int i = 0; i < ropes.size(); i++) {
            ModelPart rope = ropes.get(i);
            float rollRatio = root.zRot / rope.zRot;
            float pitchRatio = root.xRot / rope.xRot;

            rope.y -= 5F * rollRatio;
            rope.y -= 5F * pitchRatio;

            if (i == 0 || i == 3) {
                rope.z -= 5 * pitchRatio;
            }
            if (i == 2 || i == 1) {
                rope.z += 5 * pitchRatio;
            }

            if (i == 2 || i == 3) {
                rope.x -= 5 * rollRatio;
            }
            if (i == 0 || i == 1) {
                rope.x += 5 * rollRatio;
            }

            if (isBalloon) {
                rope.zScale = state.yVelocity;
                rope.xScale = 0.001F;
            } else {
                rope.xScale = 0.3F;
                rope.zScale = 0.3F;
            }
        }
    }
}
