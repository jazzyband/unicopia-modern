package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
abstract class MixinMobEntity extends LivingEntity implements Equine.Container<Creature> {
    private MixinMobEntity() { super(null, null); }

    @Shadow
    protected @Final GoalSelector goalSelector;
    @Shadow
    protected @Final GoalSelector targetSelector;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void init(EntityType<? extends Mob> entityType, Level world, CallbackInfo info) {
        get().initAi(goalSelector, targetSelector);
    }

    @Inject(method = "canReplaceCurrentItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z",
            at = @At("HEAD"), cancellable = true)
    private void onPrefersNewEquipment(ItemStack newStack, ItemStack oldStack, EquipmentSlot slot, CallbackInfoReturnable<Boolean> info) {
        if (EnchantmentUtil.prefersEquipment(newStack, oldStack)) {
            info.setReturnValue(true);
        }
    }
}
