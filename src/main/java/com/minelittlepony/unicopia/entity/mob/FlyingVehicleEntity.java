package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.collision.MultiBoundingBoxEntity;

public abstract class FlyingVehicleEntity extends Mob implements MultiBoundingBoxEntity, MagicImmune, EquineContext {

    private final UUID[] seats = new UUID[getSeatCount()];

    protected FlyingVehicleEntity(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    protected abstract int getSeatCount();

    protected abstract Vec3 getSeatPosition(int seatIndex);

    protected int getSeatFor(Entity passenger) {
        double closestDistance = Double.MAX_VALUE;
        int closestSeat = -1;

        for (int i = 0; i < seats.length; i++) {
            if (seats[i] != null) {
                if (seats[i].equals(passenger.getUUID())) {
                    return i;
                }
            } else {
                double distance = passenger.distanceToSqr(getSeatPosition(i).add(position()));
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestSeat = i;
                }

            }
        }

        return closestSeat;
    }

    @Override
    protected final Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    public final Race getSpecies() {
        return Race.UNSET;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public final boolean canBeCollidedWith(Entity other) {
        return true;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && player.getAbilities().instabuild) {
            dropEquipment(world);
            remove(RemovalReason.KILLED);
            return true;
        }
        if (super.hurtServer(world, source, amount)) {
            hurtTime = 0;
            hurtDuration = 0;
            return true;
        }
        return false;
    }

    @Override
    protected void tickDeath() {
        if (!level().isClientSide() && !isRemoved()) {
            remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < seats.length && getSeatFor(passenger) != -1;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        int seat = getSeatFor(passenger);
        if (seat == -1) {
            return Vec3.ZERO;
        }
        return getSeatPosition(seat).scale(scaleFactor);
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        java.util.List<CompoundTag> seats = compound.read("seats", CompoundTag.CODEC.listOf()).orElseGet(java.util.List::of);
        for (int i = 0; i < this.seats.length; i++) {
            this.seats[i] = i < seats.size() ? seats.get(i).read("id", UUIDUtil.AUTHLIB_CODEC).orElse(null) : null;
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        java.util.List<CompoundTag> seats = new java.util.ArrayList<>();
        for (int i = 0; i < this.seats.length; i++) {
            CompoundTag seat = new CompoundTag();
            if (this.seats[i] != null) {
                seat.store("id", UUIDUtil.AUTHLIB_CODEC, this.seats[i]);
            }
            seats.add(seat);
        }
        compound.store("seats", CompoundTag.CODEC.listOf(), seats);
    }

}
