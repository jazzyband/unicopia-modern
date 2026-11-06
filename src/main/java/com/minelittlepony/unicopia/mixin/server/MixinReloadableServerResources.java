package com.minelittlepony.unicopia.mixin.server;

import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Makes static item tags and components available while recipe previews are finalized. */
@Mixin(ReloadableServerResources.class)
abstract class MixinReloadableServerResources {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void unicopia$prepareRecipeComponents(CallbackInfo info) {
        ((ReloadableServerResources)(Object)this).updateComponentsAndStaticRegistryTags();
    }
}
