package com.minelittlepony.unicopia.mixin;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.client.ModifierTooltipRenderer;
import com.minelittlepony.unicopia.entity.effect.FoodPoisoningStatusEffect;
import com.minelittlepony.unicopia.item.DamageChecker;
import com.minelittlepony.unicopia.item.ItemStackDuck;
import com.minelittlepony.unicopia.item.component.TransientComponentMap;
import com.minelittlepony.unicopia.item.component.TransientComponentMapImpl;
import com.minelittlepony.unicopia.util.TypedActionResult;

@Mixin(ItemStack.class)
abstract class MixinItemStack implements ItemStackDuck {
    private final Supplier<TransientComponentMap> transientComponents = TransientComponentMapImpl.create(() -> (ItemStack)(Object)this);

    @Override
    public TransientComponentMap getTransientComponents() {
        return transientComponents.get();
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        getTransientComponents().setCarrier(user);
        TypedActionResult<ItemStack> result = FoodPoisoningStatusEffect.apply((ItemStack)(Object)this, user);
        if (result.result() != InteractionResult.PASS) {
            info.setReturnValue(result) ;
        }
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"))
    public void onOnStoppedUsing(Level world, LivingEntity user, int remainingUseTicks, CallbackInfo info) {
        getTransientComponents().setCarrier(null);
    }

    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    private void afterFinishUsing(Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        getTransientComponents().setCarrier(null);
    }

    @Inject(method = "getTooltipLines", at = @At("HEAD"))
    public void beforeGetTooltip(Item.TooltipContext context, @Nullable Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> info) {
        getTransientComponents().setCarrier(player);
    }

    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    public void afterGetTooltip(Item.TooltipContext context, @Nullable Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> info) {
        getTransientComponents().setCarrier(null);
    }

    @Inject(method = "addDetailsToTooltip", at = @At("HEAD"))
    public void onGetTooltip(Item.TooltipContext context, TooltipDisplay displayComponent, @Nullable Player player, TooltipFlag type, Consumer<Component> textConsumer, CallbackInfo info) {
        ItemStack self = (ItemStack)(Object)this;
        ModifierTooltipRenderer.INSTANCE.getTooltip(self, context, displayComponent, player, type, textConsumer);
    }

    @ModifyReturnValue(method = "canBeHurtBy", at = @At("RETURN"))
    private boolean onTakesDamageFrom(boolean takesDamage, DamageSource source) {
        ItemStack self = (ItemStack)(Object)this;
        return self.getItem() instanceof DamageChecker checker ? checker.takesDamageFrom(source) : takesDamage;
    }
}

@Mixin(DataComponentHolder.class)
interface MixinComponentHolder {
    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    default <T> T unicopia_onGet(T value, DataComponentType<? extends T> type) {
        return TransientComponentMap.of(this).get(type, value);
    }

    @ModifyReturnValue(method = "getOrDefault", at = @At("RETURN"))
    default <T> T unicopia_onGetOrDefault(T value, DataComponentType<? extends T> type, T fallback) {
        return TransientComponentMap.of(this).getOrDefault(type, value, fallback);
    }

    @ModifyReturnValue(method = "has", at = @At("RETURN"))
    default boolean unicopia_onContains(boolean value, DataComponentType<?> type) {
        return TransientComponentMap.of(this).contains(type, value);
    }
}
