package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

@Mixin(ItemEntity.class)
abstract class MixinItemEntity extends Entity implements IItemEntity {

    private ItemImpl caster;

    private MixinItemEntity() { super(null, null); }

    @Override
    public Equine<?> create() {
        return new ItemImpl((ItemEntity)(Object)this);
    }

    @Override
    public ItemImpl get() {
        if (caster == null) {
            caster = (ItemImpl)create();
        }
        return caster;
    }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void beforeTick(CallbackInfo info) {
        if (get().beforeUpdate()) {
            info.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void afterTick(CallbackInfo info) {
        get().tick();
    }

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At("HEAD"))
    private void onWriteCustomDataToTag(net.minecraft.world.level.storage.ValueOutput tag, CallbackInfo info) {
        tag.store("unicopia_caster", CompoundTag.CODEC, get().toNBT(level().registryAccess()));
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("HEAD"))
    private void onReadCustomDataFromTag(net.minecraft.world.level.storage.ValueInput tag, CallbackInfo info) {
        tag.read("unicopia_caster", CompoundTag.CODEC).ifPresent(data -> get().fromNBT(data, level().registryAccess()));
    }

    @Accessor("age")
    @Override
    public abstract int getAge();

    @Accessor("pickupDelay")
    @Override
    public abstract int getPickupDelay();

}
