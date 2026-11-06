package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.minelittlepony.unicopia.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
abstract class MixinLivingEntity extends Entity implements Equine.Container<Living<?>> {
    private MixinLivingEntity() { super(null, null); }

}
