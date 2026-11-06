package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.effect.SeaponyGraceStatusEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.server.level.ServerLevel;

@Mixin(targets = "net.minecraft.world.entity.monster.Guardian$GuardianAttackSelector")
abstract class MixinGuardianTargetPredicate {
    @Shadow
    private @Final Guardian guardian;

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void unicopia_excludeSeaponysGrace(@Nullable LivingEntity target, ServerLevel level, CallbackInfoReturnable<Boolean> info) {
        if (!SeaponyGraceStatusEffect.hasIre(target, guardian)) {
            info.setReturnValue(false);
        }
    }
}
