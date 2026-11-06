package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.RotatedView;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;

@Mixin(Brain.class)
abstract class MixinBrain<E extends LivingEntity> {

    @Inject(method = "tick", at = @At("HEAD"))
    public void beforeTickAi(ServerLevel world, E entity, CallbackInfo into) {
        Equine<?> eq = Equine.of(entity).orElse(null);

        if (eq instanceof Living<?> && eq.getPhysics().isGravityNegative()) {
            ((RotatedView)world).pushRotation((int)(entity.getY() + entity.getBbHeight() * 0.5F));
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void afterTickAi(ServerLevel world, E entity, CallbackInfo into) {
        ((RotatedView)world).popRotation();
    }
}
