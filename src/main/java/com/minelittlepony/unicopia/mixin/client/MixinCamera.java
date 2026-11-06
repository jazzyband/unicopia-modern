package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.client.render.spell.PortalFrameBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

@Mixin(Camera.class)
abstract class MixinCamera {
    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float unicopia$modifyFieldOfView(float original) {
        Camera camera = (Camera)(Object)this;
        if (PortalFrameBuffer.isPortalCamera(camera)) {
            return 120;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || camera.entity() != client.player) {
            return original;
        }
        return UnicopiaClient.getCamera().calculateFieldOfView(original,
                client.options.getCameraType().isFirstPerson(), client.options.fovEffectScale().get().floatValue());
    }

    @Inject(method = "setRotation", at = @At(value = "INVOKE",
            target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;", shift = At.Shift.AFTER))
    private void unicopia$applyCameraRoll(float yaw, float pitch, CallbackInfo info) {
        Camera camera = (Camera)(Object)this;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || camera.entity() != client.player || PortalFrameBuffer.isPortalCamera(camera)) {
            return;
        }
        float roll = UnicopiaClient.getCamera().calculateRoll(client.options.getCameraType().isFirstPerson(),
                client.options.fovEffectScale().get().floatValue());
        // Roll in camera space before vanilla derives the basis vectors, view matrices, and frustum.
        camera.rotation().rotateZ((float)Math.toRadians(-roll));
    }

    @ModifyVariable(method = "setRotation",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private float modifyYaw(float yaw) {
        return UnicopiaClient.getCamera().calculateYaw(yaw);
    }

    @ModifyVariable(method = "setRotation",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true)
    private float modifyPitch(float pitch) {
        return UnicopiaClient.getCamera().calculatePitch(pitch);
    }

    @ModifyReturnValue(method = "getMaxZoom",
            at = @At("RETURN"))
    private float modifyCameraDistance(float initial) {
        return UnicopiaClient.getCamera().calculateDistance(initial);
    }
}
