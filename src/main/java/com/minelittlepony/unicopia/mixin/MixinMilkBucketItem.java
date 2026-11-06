package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellSlots;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;

@Mixin(ClearAllStatusEffectsConsumeEffect.class)
abstract class MixinMilkBucketItem {
    @Inject(method = "apply", at = @At("HEAD"))
    private void onOnConsume(Level world, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> info) {
        Caster.of(user).map(Caster::getSpellSlot).ifPresent(SpellSlots::clear);
    }
}
