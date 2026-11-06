package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
abstract class MixinMouse {
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer(D)V", at = @At("HEAD"))
    private void onUpdateMouse(double tickDelta, CallbackInfo info) {
        Pony player = Pony.of(Minecraft.getInstance().player);
        if (player != null && player.getPhysics().isGravityNegative()) {
            accumulatedDX = -accumulatedDX;
            accumulatedDY = -accumulatedDY;
        }
    }
}
