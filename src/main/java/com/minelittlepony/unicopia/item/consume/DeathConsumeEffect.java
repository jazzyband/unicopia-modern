package com.minelittlepony.unicopia.item.consume;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;

public interface DeathConsumeEffect extends ConsumeEffect {
    @Override
    default boolean apply(Level world, ItemStack stack, LivingEntity user) {
        return onConsume(world, stack, user, world.damageSources().magic());
    }

    boolean onConsume(Level world, ItemStack stack, LivingEntity user, DamageSource damage);
}
