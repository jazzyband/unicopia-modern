package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.client.BatEyesApplicator;
import com.minelittlepony.unicopia.client.render.shader.ViewportShader;
import com.minelittlepony.unicopia.client.render.spell.PortalFrameBuffer;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;

@Mixin(value = GameRenderer.class, priority = Integer.MAX_VALUE)
abstract class MixinGameRenderer implements AutoCloseable, ResourceManagerReloadListener {


    @Shadow
    private @Final Minecraft minecraft;

    @Shadow
    private @Final CrossFrameResourcePool resourcePool;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void beforeRenderWorld(CallbackInfo info) {
        BatEyesApplicator.INSTANCE.enable();
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void afterRenderWorld(CallbackInfo info) {
        BatEyesApplicator.INSTANCE.disable();
    }

    @ModifyReturnValue(method = "nightVisionScale", at = @At("RETURN"))
    private static float modifyNightVisionStrength(float initial, LivingEntity entity, float tickDelta) {
        return BatEyesApplicator.getWorldBrightness(initial, entity, tickDelta);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;applyPostEffects()V", shift = Shift.AFTER))
    private void unicopia$applyCorruptionShader(CallbackInfo info) {
        if (minecraft.level != null && minecraft.player != null) {
            PortalFrameBuffer.capturePending();
            ViewportShader.INSTANCE.render(resourcePool, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        }
    }

}
