package com.minelittlepony.unicopia.mixin.client;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
interface MixinTooltipComponent {
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void onOf(FormattedCharSequence text, CallbackInfoReturnable<ClientTooltipComponent> info) {
        if (text instanceof ClientTooltipComponent c) {
            info.setReturnValue(c);
        }
    }
}
