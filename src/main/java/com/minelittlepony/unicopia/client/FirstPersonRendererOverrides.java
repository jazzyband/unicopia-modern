package com.minelittlepony.unicopia.client;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState.HandRenderSelection;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import com.google.common.base.MoreObjects;
import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.vertex.PoseStack;

public class FirstPersonRendererOverrides {
    public static final FirstPersonRendererOverrides INSTANCE = new FirstPersonRendererOverrides();

    public Optional<HandRenderSelection> getHandRenderType(Player player) {
        return Pony.of(player).getEntityInArms().map(e -> HandRenderSelection.RENDER_BOTH_HANDS);
    }

    @SuppressWarnings("unchecked")
    public boolean beforeRenderHands(ArmRenderer sender, float tickDelta, PoseStack matrices, SubmitNodeCollector vertexConsumers, LocalPlayer player, PlayerRenderState playerState, int light) {
        var root = AccessoryFeatureRenderer.FeatureRoot.of(player);
        var state = playerState.avatarRenderState;
        boolean cancelled = root != null && state != null && ((AccessoryFeatureRenderer)root.getAccessories()).beforeRenderArms(sender, matrices, vertexConsumers, state, light);

        if (cancelled) {
            return true;
        }

        if (Pony.of(player).getAnimation().renderBothArms()) {
            float swingProgress = player.getSwingAnimation(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));

            InteractionHand hand = player.getCurrentSwing() == null ? InteractionHand.MAIN_HAND : player.getCurrentSwing().hand();

            if (player.getMainHandItem().isEmpty()) {
                matrices.pushPose();
                sender.invokeRenderArmHoldingItem(matrices, vertexConsumers, light, 1 - sender.getEquipProgress(InteractionHand.MAIN_HAND, tickDelta), hand == InteractionHand.MAIN_HAND ? swingProgress : 0, player.getMainArm());
                matrices.popPose();
            }

            if (player.getOffhandItem().isEmpty()) {
                matrices.pushPose();
                sender.invokeRenderArmHoldingItem(matrices, vertexConsumers, light, 1 - sender.getEquipProgress(InteractionHand.OFF_HAND, tickDelta), hand == InteractionHand.OFF_HAND ? swingProgress : 0, player.getMainArm().getOpposite());
                matrices.popPose();
            }
        }
        return false;
    }

    public interface ArmRenderer {
        void invokeRenderArmHoldingItem(PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, float equipProgress, float swingProgress, HumanoidArm arm);

        float getEquipProgress(InteractionHand hand, float tickDelta);
    }
}
