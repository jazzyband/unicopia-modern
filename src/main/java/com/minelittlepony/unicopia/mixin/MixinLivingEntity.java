package com.minelittlepony.unicopia.mixin;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.duck.*;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntity.class)
abstract class MixinLivingEntity extends Entity implements LivingEntityDuck, Equine.Container<Living<?>> {
    @Shadow
    protected ItemStack useItem;
    @Shadow
    protected int useItemRemaining;
    @Shadow
    protected boolean dead;

    @Shadow
    private Optional<BlockPos> lastClimbablePos;

    private Equine<?> caster;

    private MixinLivingEntity() { super(null, null); }

    @Shadow
    protected abstract void setLivingEntityFlag(int mask, boolean value);

    @Override
    public Equine<?> create() {
        return new Creature((LivingEntity)(Object)this);
    }

    @Override
    public Living<?> get() {
        synchronized (this) {
            if (caster == null) {
                caster = create();
            }
            return (Living<?>)caster;
        }
    }

    @Override
    @Accessor("jumping")
    public abstract boolean isJumping();

    @Override
    @Accessor("swimAmount")
    public abstract float getLeaningPitch();

    @Override
    @Accessor("swimAmount")
    public abstract void setLeaningPitch(float pitch);

    @Override
    @Accessor("swimAmountO")
    public abstract float getLastLeaningPitch();

    @Override
    @Accessor("swimAmountO")
    public abstract void setLastLeaningPitch(float pitch);

    @Inject(method = "createLivingAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;", at = @At("RETURN"))
    private static void onCreateAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> info) {
        Creature.registerAttributes(info.getReturnValue());
    }

    @Inject(method = "onClimbable()Z", at = @At("HEAD"), cancellable = true)
    public void onIsClimbing(CallbackInfoReturnable<Boolean> info) {
        if (horizontalCollision) {
            get().chooseClimbingPos().ifPresent(pos -> {
                lastClimbablePos = Optional.of(pos);
                info.setReturnValue(true);
            });
        }
    }

    @Inject(method = "isPushable()Z", at = @At("HEAD"), cancellable = true)
    private void onIsPushable(CallbackInfoReturnable<Boolean> info) {
        Caster.of(this)
            .flatMap(c -> c.getSpellSlot().get(SpellPredicate.IS_DISGUISE))
            .map(AbstractDisguiseSpell::getAppearance)
            .map(EntityAppearance::getEntity)
            .filter(Entity::isPushable)
            .ifPresent(v -> {
                info.setReturnValue(false);
            });
    }

    @Inject(method = "hasLineOfSight(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/ClipContext$Block;Lnet/minecraft/world/level/ClipContext$Fluid;D)Z", at = @At("HEAD"), cancellable = true)
    private void onCanSee(Entity entity, ClipContext.Block shapeType, ClipContext.Fluid fluidHandling, double entityY, CallbackInfoReturnable<Boolean> info) {
        if (!get().canBeSeenBy(entity)) {
            info.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = "getFluidFallingAdjustedMovement", at = @At("RETURN"))
    private Vec3 applyFluidMovingSpeed(Vec3 speed, double gravity, boolean falling, Vec3 motion) {
        return get().adjustMovementSpeedInWater(speed).orElse(speed);
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

    @Dynamic("Compiler-generated class-init() method")
    @Inject(method = "<clinit>()V", at = @At("RETURN"), remap = false)
    private static void clinit(CallbackInfo info) {
        Creature.boostrap();
    }

    @Inject(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
    private void onDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        get().onDamage(world, source, amount).ifPresent(info::setReturnValue);
    }

    @Inject(method = "checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z", at = @At("RETURN"))
    private void onOnDeath(DamageSource source, CallbackInfoReturnable<Boolean> info) {
        if (!isRemoved() && !dead) {
            get().onDeath(source, info.getReturnValue());
        }
    }

    @Inject(method = "setLastHurtMob(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void onOnAttacking(Entity target, CallbackInfo info) {
        get().onAttacking(target);
    }

    @ModifyVariable(method = "causeFallDamage(DFLnet/minecraft/world/damagesource/DamageSource;)Z", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double onHandleFallDamage(double distance, double distanceAgain, float damagePerDistance, DamageSource cause) {
        return get().onImpact(distance, damagePerDistance, cause);
    }

    @Inject(method = "dismountVehicle(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void onOnDismounted(Entity vehicle, CallbackInfo info) {
        get().onDismounted(vehicle);
    }

    @ModifyReturnValue(method = "isSensitiveToWater()Z", at = @At("RETURN"))
    private boolean onCanBeHurtByWater(boolean hurt) {
        TriState hurtByWater = get().canBeHurtByWater();
        if (hurtByWater != TriState.DEFAULT) {
            return hurtByWater.get();
        }
        return hurt;
    }

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At("HEAD"))
    private void onWriteCustomDataToTag(net.minecraft.world.level.storage.ValueOutput tag, CallbackInfo info) {
        tag.store("unicopia_caster", CompoundTag.CODEC, get().toNBT(level().registryAccess()));
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("HEAD"))
    private void onReadCustomDataFromTag(net.minecraft.world.level.storage.ValueInput tag, CallbackInfo info) {
        tag.read("unicopia_caster", CompoundTag.CODEC).ifPresent(data -> get().fromNBT(data, level().registryAccess()));
    }

    @Override
    public void updateItemUsage(InteractionHand hand, ItemStack stack, int time) {
        useItem = stack;
        useItemRemaining = time;

        if (!level().isClientSide()) {
            setLivingEntityFlag(1, !stack.isEmpty());
            setLivingEntityFlag(2, hand == InteractionHand.OFF_HAND);
        }
    }
}
