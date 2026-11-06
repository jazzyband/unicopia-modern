package com.minelittlepony.unicopia.mixin.gravity;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.mojang.blaze3d.audio.Channel;
import net.minecraft.world.phys.Vec3;

@Mixin(Channel.class)
abstract class MixinSoundSource {
    @Shadow
    private @Final int source;

    @Nullable
    private Vec3 unicopiaOriginalPos = null;

    @ModifyVariable(method = "setSelfPosition", at = @At("HEAD"))
    private Vec3 modifyPosition(Vec3 pos) {
        unicopiaOriginalPos = pos;
        return UnicopiaClient.getAdjustedSoundPosition(pos, isRelative());
    }

    @Inject(method = "setRelative", at = @At("RETURN"))
    private void onSetRelative(boolean relative, CallbackInfo info) {
        if (unicopiaOriginalPos != null) {
            ((Channel)(Object)this).setSelfPosition(unicopiaOriginalPos);
            unicopiaOriginalPos = null;
        }
    }

    @Unique
    private boolean isRelative() {
        return AL10.alGetSourcei(source, AL10.AL_SOURCE_RELATIVE) == Channel.BUFFER_DURATION_SECONDS /*(1)*/;
    }
}
