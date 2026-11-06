package com.minelittlepony.unicopia.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntitySwingAccessor {
    @Accessor("swingState")
    LivingEntity.SwingState unicopia$getSwingState();
}
