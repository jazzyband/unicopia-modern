package com.minelittlepony.unicopia.client.render;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.client.FirstPersonRendererOverrides.ArmRenderer;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.feature.Feature;

public class HeldEntityFeatureRenderer<S extends HumanoidRenderState, E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<S> {
    public HeldEntityFeatureRenderer(RenderLayerParent<S, ? extends HumanoidModel<S>> context) {
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, S entity, float limbAngle, float limbDistance) {
        CasterState state = CasterState.of(entity);
        CasterState.PassengerState<?, ?> carriedEntity = state.carriedEntity;
        if (carriedEntity.state != null) {
            matrices.pushPose();
            matrices.rotate(Axis.XP.rotationDegrees(180 - state.leanAmount * 90));

            matrices.translate(carriedEntity.carryPosition.x, carriedEntity.carryPosition.y, carriedEntity.carryPosition.z);
            if (carriedEntity.isPony) {
                matrices.rotate(Axis.YP.rotationDegrees(90));
            }

            carriedEntity.render(matrices, vertexConsumers, light);
            matrices.popPose();
        }
    }

    @Override
    public void renderArm(PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, S entity, ModelPart arm, HumanoidArm side) {

    }

    @Override
    public boolean beforeRenderArms(ArmRenderer sender, PoseStack matrices, SubmitNodeCollector vertexConsumers, S entity, int light) {
        CasterState state = CasterState.of(entity);
        CasterState.PassengerState<?, ?> carriedEntity = state.carriedEntity;
        if (carriedEntity.state != null) {
            matrices.pushPose();
            matrices.translate(carriedEntity.viewportPosition.x(), carriedEntity.viewportPosition.y(), carriedEntity.viewportPosition.z());
            matrices.translate(0, -1.3F, carriedEntity.isPony ? -1.9F : -1.3F);
            matrices.rotate(Axis.XP.rotationDegrees(carriedEntity.isPony ? 33 : 13));
            matrices.rotate(Axis.YP.rotationDegrees(carriedEntity.isPony ? 180 : 90));

            carriedEntity.render(matrices, vertexConsumers, light);
            matrices.popPose();

            float equipProgress = 1 - sender.getEquipProgress(InteractionHand.MAIN_HAND, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));

            matrices.pushPose();
            sender.invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, net.minecraft.client.Minecraft.getInstance().player == null ? 0 : net.minecraft.client.Minecraft.getInstance().player.getSwingAnimation(0), HumanoidArm.LEFT);
            matrices.popPose();
            matrices.pushPose();
            sender.invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, net.minecraft.client.Minecraft.getInstance().player == null ? 0 : net.minecraft.client.Minecraft.getInstance().player.getSwingAnimation(0), HumanoidArm.RIGHT);
            matrices.popPose();
            return true;
        }

        return false;
    }

    public static Vector3f getViewportPosition(Living<?> entity, Living<?> passenger, float tickDelta) {
        float handSwingProgress = entity.asEntity().getSwingAnimation(tickDelta);
        float f = -0.4f * Mth.sin(Mth.sqrt(handSwingProgress) * (float)Math.PI);
        float g = 0.2f * Mth.sin(Mth.sqrt(handSwingProgress) * ((float)Math.PI * 2));
        float h = -0.2f * Mth.sin(handSwingProgress * (float)Math.PI);
        return new Vector3f(f, g, h);
    }

    public static Vec3 getCarryPosition(Living<?> entity, Living<?> passenger) {
        float passengerHeight = MineLPDelegate.getInstance().getPonyHeight(passenger.asEntity()) / 2F;
        float carrierHeight = MineLPDelegate.getInstance().getPonyHeight(entity.asEntity()) / 5F;

        if (entity instanceof Pony pony
                && MineLPDelegate.getInstance().getPlayerPonyRace(pony.asEntity()).isEquine()
                && pony.getPhysics().isFlying()) {
            return new Vec3(0,
                    -carrierHeight * 10 - passengerHeight * 2,
                    0
            );
        }

        return new Vec3(0,
                -passengerHeight - carrierHeight,
                entity.asEntity().getBbWidth()
        );
    }
}
