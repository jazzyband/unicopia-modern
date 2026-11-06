package com.minelittlepony.unicopia.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.duck.ServerPlayerEntityDuck;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.player.SpawnLocator;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.phys.AABB;

@Mixin(ServerPlayer.class)
abstract class MixinServerPlayerEntity extends Player implements ContainerListener, Equine.Container<Pony>, ServerPlayerEntityDuck {
    MixinServerPlayerEntity() {super(null, null);}

    @Override
    @Accessor("isChangingDimension")
    public abstract void setPreventMotionChecks(boolean enabled);

    @SuppressWarnings("unchecked")
    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("HEAD"))
    private void onCopyFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo info) {
        get().copyFrom(((Equine.Container<Pony>)oldPlayer).get(), alive);
    }

    @Inject(method = {
            "startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;",
            "lambda$0()Lcom/mojang/datafixers/util/Either;"
    }, at = @At(
            value = "FIELD",
            target = "net/minecraft/entity/player/PlayerEntity$SleepFailureReason.NOT_POSSIBLE_NOW:Lnet/minecraft/entity/player/PlayerEntity$SleepFailureReason;"
    ), cancellable = true, require = 0)
    private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> info) {
        // Day/Night check is overriden using EntitySleepEvents.ALLOW_SLEEP_TIME in NocturnalSleepManager
        if (get().canSleepNow(level().isBrightOutside()) == InteractionResult.FAIL) {
            this.sendOverlayMessage(Component.translatable("block.unicopia.bed.no_sleep.nocturnal"));

            info.setReturnValue(Either.left(Player.BedSleepingProblem.OTHER_PROBLEM));
        }
    }

    @Inject(method = "awardKillScore(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)V",
            at = @At("TAIL"))
    private void onUpdateKilledAdvancementCriterion(Entity entityKilled, DamageSource damageSource, CallbackInfo info) {
        get().onKill(entityKilled, damageSource);
    }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At("HEAD"))
    private void onStartRiding(Entity entity, boolean force, boolean preserveMotion, CallbackInfoReturnable<Boolean> info) {
        get().getPhysics().cancelFlight(true);
    }

    @com.llamalad7.mixinextras.injector.ModifyReturnValue(method = "adjustSpawnLocation", at = @At("RETURN"))
    private BlockPos unicopia_adjustSpawnPosition(BlockPos result, ServerLevel world, BlockPos basePos) {
        if (result == null) {
            return null;
        }
        return SpawnLocator.findAdjustedOverworldSpawn(world, (Player)(Object)this,
                getBoundingBox(), basePos, result.getX(), result.getZ(),
                args -> result);
    }
}
