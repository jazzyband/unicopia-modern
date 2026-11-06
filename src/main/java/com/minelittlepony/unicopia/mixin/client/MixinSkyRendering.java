package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;

@Mixin(value = SkyRenderer.class, priority = 1001)
abstract class MixinSkyRendering {
    @Inject(method = "renderSunMoonAndStars", at = @At(
        value = "INVOKE",
        target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
        ordinal = 0,
        shift = At.Shift.AFTER
    ))
    private void onRenderCelestialBodies(CallbackInfo info, @Local(argsOnly = true) PoseStack matrices) {
        matrices.rotate(Axis.XP.rotationDegrees(UnicopiaClient.getInstance().getSkyAngleDelta(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false))));
        matrices.rotate(Axis.YP.rotationDegrees(UnicopiaClient.getInstance().tangentalSkyAngle.getValue()));
    }
}
