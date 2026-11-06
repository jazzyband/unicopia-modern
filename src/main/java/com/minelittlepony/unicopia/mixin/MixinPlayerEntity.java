package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.annotation.CaptureArg;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Player.class)
abstract class MixinPlayerEntity extends LivingEntity implements Equine.Container<Pony> {
    private MixinPlayerEntity() { super(null, null); }
    @Override
    public Equine<?> create() {
        return new Pony((Player)(Object)this);
    }

    @ModifyReturnValue(method = "createAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;", at = @At("RETURN"))
    private static AttributeSupplier.Builder onCreateAttributes(AttributeSupplier.Builder builder) {
        Pony.registerAttributes(builder);
        return builder;
    }

    @ModifyVariable(method = "actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    protected float modifyDamageAmount(float amount, @CaptureArg ServerLevel world, @CaptureArg DamageSource source) {
        return get().modifyDamage(source, amount).orElse(amount);
    }

    @Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
    private void onTrySleep(net.minecraft.world.level.block.AbstractBedBlock bed, net.minecraft.world.level.block.state.BlockState state,
            net.minecraft.world.attribute.BedRule rule, BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> info) {
        if (!level().isClientSide()) {
            get().trySleep(pos).ifPresent(reason -> {
                ((Player)(Object)this).sendOverlayMessage(reason);

                info.setReturnValue(Either.left(Player.BedSleepingProblem.OTHER_PROBLEM));
            });
        }
    }

    @Inject(method = "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F",
            at = @At("RETURN"),
            cancellable = true)
    private void onGetBlockBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> info) {
        info.setReturnValue(info.getReturnValue() * get().getBlockBreakingSpeed());
    }

    @Override
    protected int decreaseAirSupply(int air) {
        if (EquinePredicates.PLAYER_SEAPONY.test(this)) {
            return super.increaseAirSupply(air);
        }
        return super.decreaseAirSupply(air);
    }

    @Override
    protected int increaseAirSupply(int air) {
        if (EquinePredicates.PLAYER_SEAPONY.test(this)) {
            return super.decreaseAirSupply(air);
        }
        return super.increaseAirSupply(air);
    }
}
