package com.minelittlepony.unicopia.mixin.client;

import java.util.Map;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLanguage.class)
abstract class MixinTranslationStorage {
    @Shadow
    private @Final Map<String, String> storage;

    @Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true)
    public void onGet(String key, String fallback, CallbackInfoReturnable<String> info) {
        if (key != null && key.contains(".pegasus") && !storage.containsKey(key)) {
            info.setReturnValue(storage.getOrDefault(key.replace(".pegasus", ""), fallback));
        }
    }

    @Inject(method = "has", at = @At("RETURN"), cancellable = true)
    public void onHasTranslation(String key, CallbackInfoReturnable<Boolean> info) {
        if (key != null
                && key.contains(".pegasus")
                && !info.getReturnValueZ()
                && storage.containsKey(key.replace(".pegasus", ""))) {
            info.setReturnValue(true);
        }
    }
}