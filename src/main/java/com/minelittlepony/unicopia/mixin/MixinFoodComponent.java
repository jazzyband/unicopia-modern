package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;

@Mixin(FoodProperties.class)
abstract class MixinFoodComponent {
    @Inject(method = "onConsume", at = @At("TAIL"))
    public void onOnConsume(Level world, LivingEntity user, ItemStack stack, Consumable consumable, CallbackInfo info) {
        Pony.of(user).ifPresent(pony -> pony.onEat(stack, stack.get(DataComponents.FOOD)));
    }
}
