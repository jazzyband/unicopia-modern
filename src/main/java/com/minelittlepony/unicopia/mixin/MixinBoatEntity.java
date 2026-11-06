package com.minelittlepony.unicopia.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.material.Fluid;

@Mixin(AbstractBoat.class)
abstract class MixinBoatEntity extends VehicleEntity implements Leashable, LavaAffine {
    private static final EntityDataAccessor<Boolean> IS_LAVA_BOAT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);

    MixinBoatEntity() { super(null, null); }

    @ModifyExpressionValue(
            method = {
                    "getWaterLevelAbove",
                    "checkInWater",
                    "isUnderwater",
                    "checkFallDamage",
                    "canAddPassenger"
            },
            at = @At(
                    value = "FIELD",
                    target = "net/minecraft/registry/tag/FluidTags.WATER:Lnet/minecraft/registry/tag/TagKey;",
                    opcode = Opcodes.GETSTATIC
            ),
            require = 0 // Forge
    )
    private TagKey<Fluid> redirectFluidTag(TagKey<Fluid> tag) {
        return isLavaAffine() ? FluidTags.LAVA : tag;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void onWriteCustomDataToNbt(net.minecraft.world.level.storage.ValueOutput nbt, CallbackInfo info) {
        nbt.putBoolean("IsLavaAffine", isLavaAffine());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void onReadCustomDataFromNbt(net.minecraft.world.level.storage.ValueInput nbt, CallbackInfo info) {
        setLavaAffine(nbt.getBooleanOr("IsLavaAffine", false));
    }

    @Inject(method = "defineSynchedData", at = @At("HEAD"))
    private void onInitDataTracker(Builder builder, CallbackInfo info) {
        builder.define(IS_LAVA_BOAT, false);
    }

    @Override
    public void setLavaAffine(boolean lavaAffine) {
        entityData.set(IS_LAVA_BOAT, lavaAffine);
        if (lavaAffine) {
            ParticleUtils.spawnParticles(ParticleTypes.CLOUD, this, 10);
        }
    }

    @Override
    public boolean isLavaAffine() {
        return entityData.get(IS_LAVA_BOAT);
    }
}
