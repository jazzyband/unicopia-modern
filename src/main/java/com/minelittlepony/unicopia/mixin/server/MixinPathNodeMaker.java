package com.minelittlepony.unicopia.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.InteractionManager;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.NodeEvaluator;

@Mixin(NodeEvaluator.class)
abstract class MixinPathNodeMaker {
    @Inject(method = "prepare", at = @At("HEAD"))
    private void onInit(PathNavigationRegion cachedWorld, Mob entity, CallbackInfo info) {
        InteractionManager.getInstance().setEquineContext(EquineContext.of(entity));
    }

    @Inject(method = "done", at = @At("HEAD"))
    private void onClear(CallbackInfo info) {
        InteractionManager.getInstance().clearEquineContext();
    }
}
