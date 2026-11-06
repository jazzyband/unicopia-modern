package com.minelittlepony.unicopia.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.collision.MultiBoundingBoxEntity;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.util.Tickable;

public class Transportation<T extends LivingEntity> implements Tickable {

    private final Living<T> living;

    @Nullable
    private MultiBoundingBoxEntity vehicle;
    @Nullable
    private Entity vehicleEntity;
    @Nullable
    private AABB vehicleBox;

    private int ticksInVehicle;

    private Vec3 lastVehiclePosition = Vec3.ZERO;

    Transportation(Living<T> living) {
        this.living = living;
    }

    public <E extends Entity & MultiBoundingBoxEntity> void setVehicle(@Nullable E vehicle) {
        this.vehicle = vehicle;
        this.vehicleEntity = vehicle;
        updatePreviousPosition();
    }

    @Override
    public void tick() {
        if (vehicle != null) {
            ticksInVehicle++;
        } else {
            ticksInVehicle = 0;
        }

        if (ticksInVehicle > 20 && vehicle instanceof AirBalloonEntity b
                && !b.onGround() && b.hasBalloon() && b.hasBurner() && b.isAscending()) {
            UCriteria.RIDE_BALLOON.trigger(living.asEntity());
        }
    }

    public void updatePreviousPosition() {
        vehicleBox = getVehicleBox();
        lastVehiclePosition = vehicleEntity == null ? Vec3.ZERO : vehicleEntity.position();
        Entity entity = living.asEntity();
        if (vehicleBox != null && living.asEntity().getBoundingBox().intersects(vehicleBox.inflate(0.001, 0.5001, 0.001))) {
            entity.setOnGround(true);
            entity.resetFallDistance();
            entity.verticalCollision = true;
            entity.verticalCollisionBelow = true;
            entity.syncVelocity = true;
        }
    }

    public void onMove(MoverType movementType) {
        if (vehicleBox == null || vehicleEntity == null) {
            return;
        }

        Entity entity = living.asEntity();

        AABB passengerBox = entity.getBoundingBox().inflate(0.001);
        Vec3 vehicleMovement = vehicleEntity.position().subtract(lastVehiclePosition);

        List<VoxelShape> shapes = new ArrayList<>();
        vehicle.getCollissionShapes(CollisionContext.of(entity), shapes::add);
        vehicleMovement = vehicleMovement.add(vehicleEntity.getDeltaMovement());
        vehicleMovement = Entity.collideBoundingBox(entity, vehicleMovement, passengerBox, entity.level(), shapes);

        Vec3 newPos = entity.position().add(vehicleMovement);

        if (!vehicleEntity.onGround()) {
            // surface check to prevent the player from floating
            if (newPos.y() > vehicleBox.minY + 0.1 || newPos.y() < vehicleBox.minY + 0.1) {
                newPos = new Vec3(newPos.x(), vehicleBox.minY + 0.01, newPos.z());
            }
            // containment checks to prevent the player from falling out of the basket when in flight
            if (newPos.y() < vehicleEntity.position().y() + 3) {
                double maxDeviation = 0.1;
                double z = Mth.clamp(newPos.z(), vehicleBox.minZ + maxDeviation, vehicleBox.maxZ - maxDeviation);
                double x = Mth.clamp(newPos.x(), vehicleBox.minX + maxDeviation, vehicleBox.maxX - maxDeviation);

                newPos = new Vec3(x, newPos.y(), z);
            }

            entity.setPos(newPos);
            entity.syncPacketPositionCodec(newPos.x, newPos.y, newPos.z);
            entity.setDeltaMovement(Vec3.ZERO);
        }

        entity.setOnGround(true);
        entity.resetFallDistance();
        entity.verticalCollision = true;
        entity.verticalCollisionBelow = true;

        if (entity.moveDist > ((EntityDuck)entity).getNextStepSoundDistance()) {
            entity.moveDist -= 0.5;
            entity.playSound(vehicle.getWalkedOnSound(entity.getY()), 0.5F, 1);
            if (!entity.isDiscrete()) {
                entity.level().gameEvent(entity, GameEvent.STEP, entity.blockPosition());
            }
        }
    }

    @Nullable
    private AABB getVehicleBox() {
        if (!EntitySelector.NO_SPECTATORS.test(living.asEntity())) {
            return null;
        }
        if (vehicle == null) {
            return null;
        }

        AABB entityBox = living.asEntity().getBoundingBox().expandTowards(living.asEntity().getDeltaMovement());
        for (AABB box : vehicle.getGravityZoneBoxes()) {
            if (entityBox.intersects(box.inflate(0.001).expandTowards(vehicleEntity.getDeltaMovement().scale(1)))) {
                return box;
            }
        }

        setVehicle(null);
        return null;
    }
}
