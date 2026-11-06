package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Living;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;

@Mixin(Brain.class)
abstract class MixinBrain<E extends LivingEntity> {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(ServerLevel world, E entity, CallbackInfo info) {
        if (Living.living(entity) instanceof Creature c && c.isDiscorded()) {
            info.cancel();
        }
    }
}
