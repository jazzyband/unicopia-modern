package com.minelittlepony.unicopia.mixin;

import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.MoveFunction;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.network.track.DataTrackerManager;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;

@Mixin(Entity.class)
abstract class MixinEntity implements EntityDuck, Trackable {
    @Nullable
    private transient Caster<?> host;

    private DataTrackerManager dataTrackerManager;

    @Override
    @Nullable
    public Caster<?> getHost() {
        return host;
    }

    @Override
    public void setHost(Caster<?> host) {
        this.host = host;
    }

    @Override
    public DataTrackerManager getDataTrackers() {
        synchronized (this) {
            if (dataTrackerManager == null) {
                dataTrackerManager = new DataTrackerManager((Entity)(Object)this);
                // ensure lazy registration happens
                if (this instanceof Equine.Container<?> eq) {
                    eq.get();
                }
            }
            return dataTrackerManager;
        }
    }

    @Override
    @Accessor
    public abstract void setRemovalReason(RemovalReason reason);

    @Override
    @Accessor
    public abstract void setVehicle(Entity vehicle);

    @Accessor
    public abstract float getNextStep();

    @Override
    public boolean isLavaAffine() {
        Entity self = (Entity)(Object)this;
        return self.isPassenger() && self.getVehicle() instanceof LavaAffine affine && affine.isLavaAffine();
    }

    @ModifyReturnValue(method = "getDimensions", at = @At("RETURN"))
    private net.minecraft.world.entity.EntityDimensions unicopia_playerDimensions(
            net.minecraft.world.entity.EntityDimensions original, net.minecraft.world.entity.Pose pose) {
        if ((Object)this instanceof net.minecraft.world.entity.player.Player player) {
            var pony = com.minelittlepony.unicopia.entity.player.Pony.of(player);
            if (pony != null) {
                return pony.getMotion().getDimensions().calculateDimensions(original);
            }
        }
        return original;
    }

    @Inject(method = "isInvulnerable", at = @At("HEAD"), cancellable = true)
    private void onIsInvulnerable(CallbackInfoReturnable<Boolean> info) {
        Living<?> living = Living.living((Entity)(Object)this);
        if (living != null && living.isInvulnerable()) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void onIsFireImmune(CallbackInfoReturnable<Boolean> info) {
        if (isLavaAffine() || (this instanceof Equine.Container c) && c.get().getCompositeRace().includes(Race.KIRIN)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "isDiscrete", at = @At("HEAD"), cancellable = true)
    private void onIsSneaky(CallbackInfoReturnable<Boolean> info) {
        if (EquinePredicates.PLAYER_KIRIN.test((Entity)(Object)this) && !EquinePredicates.RAGING.test((Entity)(Object)this)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "getMaxAirSupply", at = @At("HEAD"), cancellable = true)
    private void onGetMaxAir(CallbackInfoReturnable<Integer> info) {
        if (EquinePredicates.PLAYER_KIRIN.test((Entity)(Object)this)) {
            info.setReturnValue(150);
        }
    }

    @Inject(method = "displayFireAnimation", at = @At("HEAD"), cancellable = true)
    private void onDoesRenderOnFire(CallbackInfoReturnable<Boolean> info) {
        if (EquinePredicates.RAGING.test((Entity)(Object)this)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void updatePassengerPosition(Entity passenger, MoveFunction positionUpdater, CallbackInfo info) {
        if (Living.getOrEmpty((Entity)(Object)this).filter(l -> l.onUpdatePassengerPosition(passenger, positionUpdater)).isPresent()) {
            info.cancel();
        }
    }

    @Inject(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void onDropStack(ServerLevel world, ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> info) {
        if (getHost() != null) {
            info.setReturnValue(null);
        }
    }

    @ModifyReturnValue(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"))
    private @Nullable ItemEntity onDropItem(@Nullable ItemEntity item) {
        if (item != null) {
            Living.getOrEmpty((Entity)(Object)this).ifPresent(l -> l.onDropItem(item));
        }
        return item;
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void beforeMove(MoverType movementType, Vec3 movement, CallbackInfo info) {
        Living<?> living = Living.living((Entity)(Object)this);
        if (living != null) {
            living.getTransportation().updatePreviousPosition();
        }
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void afterMove(MoverType movementType, Vec3 movement, CallbackInfo info) {
        Living<?> living = Living.living((Entity)(Object)this);
        if (living != null) {
            living.getTransportation().onMove(movementType);
        }
    }
}
