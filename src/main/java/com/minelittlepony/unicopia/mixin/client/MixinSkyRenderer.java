package com.minelittlepony.unicopia.mixin.client;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.client.render.WorldRenderDelegate;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;

@Mixin(SkyRenderer.class)
abstract class MixinSkyRenderer {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void unicopia$overrideSkyColor(ClientLevel level, float tickDelta, Camera camera, SkyRenderState state, CallbackInfo info) {
        WorldRenderDelegate.INSTANCE.getSkyColor(tickDelta).ifPresent(color ->
                state.skyColor = new Vector3f((float)color.x, (float)color.y, (float)color.z));
    }
}
