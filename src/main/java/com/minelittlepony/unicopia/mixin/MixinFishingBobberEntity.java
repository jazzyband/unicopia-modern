package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.minelittlepony.unicopia.item.BaitedFishingRodItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(FishingHook.class)
abstract class MixinFishingBobberEntity extends Projectile implements BaitedFishingRodItem.BaitedFishingBobber {
    private MixinFishingBobberEntity() { super(null, null); }

    @Nullable
    private Item rodType;

    @Nullable
    @Override
    public Item getRodType() {
        return rodType;
    }

    @Override
    public void setRodType(Item rodType) {
        this.rodType = rodType;
    }

    @ModifyExpressionValue(method = "shouldStopFishing", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"),
            @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getOffhandItem()Lnet/minecraft/world/item/ItemStack;")
    }, expect = 2)
    private ItemStack replaceFishingRodItem(ItemStack initialStack) {
        if (rodType != null) {
            return initialStack.is(rodType) ? Items.FISHING_ROD.getDefaultInstance() : ItemStack.EMPTY;
        }
        return initialStack;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void onWriteCustomDataToNbt(ValueOutput nbt, CallbackInfo info) {
        nbt.store("rodType", BuiltInRegistries.ITEM.byNameCodec(), rodType == null ? Items.FISHING_ROD : rodType);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void onReadCustomDataFromNbt(ValueInput nbt, CallbackInfo info) {
        rodType = nbt.read("rodType", BuiltInRegistries.ITEM.byNameCodec()).orElse(null);
    }
}
