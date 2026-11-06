package com.minelittlepony.unicopia.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.MobBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobBucketItem.class)
public interface MixinEntityBucketItem {
    @Accessor
    EntityType<?> getType();
    @Accessor
    SoundEvent getEmptySound();
}
