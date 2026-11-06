package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.PowderSnowBlock;

@Mixin(PowderSnowBlock.class)
abstract class MixinPowderSnowBlock {
    @Inject(method = "canEntityWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
    private static void onCanWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (Pony.of(entity).filter(pony -> pony.getCompositeRace().canFly()).isPresent()) {
            info.setReturnValue(true);
        }
    }
}
