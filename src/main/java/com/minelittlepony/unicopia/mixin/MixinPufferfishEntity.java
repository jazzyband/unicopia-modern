package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.effect.SeaponyGraceStatusEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fish.Pufferfish;

@Mixin(Pufferfish.class)
abstract class MixinPufferfishEntity {
    @ModifyReturnValue(method = "lambda$static$0(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/server/level/ServerLevel;)Z", at = @At("RETURN"))
    private static boolean unicopia_excludeSeaponysGrace(boolean matched, LivingEntity entity) {
        return matched && !SeaponyGraceStatusEffect.hasGrace(entity);
    }
}
