package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.minelittlepony.unicopia.client.render.WorldRenderDelegate;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;

@Mixin(FogRenderer.class)
abstract class MixinBackgroundRenderer {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void unicopia_adjustFog(Camera camera, int renderDistance, DeltaTracker delta,
            float fogDistance, ClientLevel level, CallbackInfoReturnable<FogData> info) {
        WorldRenderDelegate.INSTANCE.applyFog(camera, info.getReturnValue());
    }
}
