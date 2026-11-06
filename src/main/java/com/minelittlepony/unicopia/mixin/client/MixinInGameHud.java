package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.client.gui.HudEffects;
import com.minelittlepony.unicopia.client.gui.UHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;

@Mixin(Hud.class)
abstract class MixinInGameHud {

    @Shadow
    abstract Player getCameraPlayer();

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onRender(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo info) {
        HudEffects.tryApply(getCameraPlayer(), tickCounter, true);
        UHud.INSTANCE.render(context, tickCounter);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void afterRender(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo info) {
        HudEffects.tryApply(getCameraPlayer(), tickCounter, false);
    }

    /*@ModifyArg(
            method = "drawHeart",
            at = @At(value = "INVOKE", target = "net/minecraft/client/gui/DrawContext.drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"),
            index = 0
    )
    private Identifier adjustHeartTexture(Identifier texture, @Local InGameHud.HeartType heartsType, @Local(ordinal = 0) boolean hardcore, @Local(ordinal = 1) boolean blinking, @Local(ordinal = 2) boolean half) {
        return UHud.getHeartTexture(heartsType, texture, hardcore, blinking, half);
    }*/
}

@Mixin(targets = "net.minecraft.client.gui.Hud$HeartType")
abstract class HeartType {
    @ModifyReturnValue(
        method = "forPlayer",
        at = @At("RETURN"))
    private static Hud.HeartType onFromPlayerState(Hud.HeartType heartsType, Player player) {
        return (Hud.HeartType)UHud.getHeartsType(player, heartsType);
    }
}
