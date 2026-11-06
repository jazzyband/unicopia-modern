package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

@Mixin(TargetingConditions.class)
abstract class MixinTargetPredicate {
    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void onTest(ServerLevel world, @Nullable LivingEntity tester, LivingEntity target, CallbackInfoReturnable<Boolean> info) {
        Pony.of(target).ifPresent(pony -> {
            if (!pony.canBeSeenBy(tester)) {
                info.setReturnValue(false);
            }
        });
    }
}
