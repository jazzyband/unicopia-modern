package com.minelittlepony.unicopia.mixin.datafix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.DataFixerBuilder;
import net.minecraft.util.datafix.DataFixers;

@Mixin(DataFixers.class)
abstract class MixinSchemas {
    @Inject(method = "addFixers", at = @At("TAIL"))
    private static void unicopia_build(DataFixerBuilder builder, net.minecraft.util.filefix.FileFixerUpper.Builder fileBuilder, CallbackInfo info) {
        com.minelittlepony.unicopia.datafix.Schemas.build(builder);
    }
}
