package com.minelittlepony.unicopia.mixin;

import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Blaze.class)
public interface MixinBlazeEntity {
    @Invoker
    void invokeSetCharged(boolean active);
}
