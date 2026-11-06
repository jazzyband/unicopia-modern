package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.client.FirstPersonRendererOverrides;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
abstract class MixinHeldItemRenderer implements FirstPersonRendererOverrides.ArmRenderer {
    @Unique private PlayerRenderState unicopia_playerState;
    @Unique private FirstPersonHandsAndItemsRenderState unicopia_handState;
    @Unique private float unicopia_tickDelta;

    @Invoker("submitArmWithItem")
    protected abstract void unicopia_submitArmWithItem(PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState handState, float tickDelta, float pitch,
            InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress,
            PoseStack matrices, SubmitNodeCollector vertices, int light);

    @Override
    public void invokeRenderArmHoldingItem(PoseStack matrices, SubmitNodeCollector vertices,
            int light, float equipProgress, float swingProgress, HumanoidArm arm) {
        InteractionHand hand = Minecraft.getInstance().player.getMainArm() == arm
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        unicopia_submitArmWithItem(unicopia_playerState, unicopia_handState, unicopia_tickDelta,
                0, hand, swingProgress, ItemStack.EMPTY, equipProgress, matrices, vertices, light);
    }

    @Override
    public float getEquipProgress(InteractionHand hand, float tickDelta) {
        return hand == InteractionHand.MAIN_HAND
                ? Mth.lerp(tickDelta, unicopia_handState.oldMainHandHeight, unicopia_handState.mainHandHeight)
                : Mth.lerp(tickDelta, unicopia_handState.oldOffHandHeight, unicopia_handState.offHandHeight);
    }

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void unicopia_beforeSubmit(float tickDelta, PoseStack matrices, SubmitNodeCollector vertices,
            PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState handState, CallbackInfo info) {
        unicopia_playerState = playerState;
        unicopia_handState = handState;
        unicopia_tickDelta = tickDelta;
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        FirstPersonRendererOverrides.INSTANCE.getHandRenderType(player)
                .ifPresent(selection -> handState.handRenderSelection = selection);
        if (FirstPersonRendererOverrides.INSTANCE.beforeRenderHands(this, tickDelta, matrices,
                vertices, player, playerState, playerState.avatarRenderState.lightCoords)) {
            info.cancel();
        }
    }
}
