package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.item.ItemDuck;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.ItemFrame;

@Mixin(BlockAttachedEntity.class)
abstract class MixinAbstractDecorationEntity {
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void beforeTick(CallbackInfo info) {
        final Object _this = this;
        if (_this instanceof ItemFrame self) {
            ItemDuck.of(self.getItem()).inFrameTick(self);
        }
    }
}
