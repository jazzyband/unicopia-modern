package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.damage.UDamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import static net.minecraft.network.syncher.SynchedEntityData.Builder;;

public abstract class StationaryObjectEntity extends Entity implements UDamageSources, MagicImmune {
    private static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(StationaryObjectEntity.class, EntityDataSerializers.FLOAT);

    public StationaryObjectEntity(EntityType<?> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        builder.define(HEALTH, getMaxHealth());
    }

    public abstract float getMaxHealth();

    public final float setHealth(float health) {
        health = Mth.clamp(health, 0, getMaxHealth());
        entityData.set(HEALTH, health);
        return health;
    }

    public final float getHealth() {
        return entityData.get(HEALTH);
    }

    public final boolean isDead() {
        return isRemoved() || getHealth() <= 0;
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float damage) {
        if (!isDead()) {
            if (setHealth(getHealth() - damage) <= 0) {
                kill(world);
                onKilled(source);
            } else {
                onHurt();
            }
        }
        return false;
    }

    protected void onKilled(DamageSource source) {

    }

    protected void onHurt() {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput compound) {
        setHealth(compound.getFloatOr("health", getMaxHealth()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput compound) {
        compound.putFloat("health", getHealth());
    }

    @Override
    public final boolean isPickable() {
        return true;
    }

    @Override
    public final Level asWorld() {
        return level();
    }
}
