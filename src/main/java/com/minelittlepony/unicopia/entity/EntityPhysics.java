package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.util.Copyable;
import com.minelittlepony.unicopia.util.Tickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityPhysics<T extends Entity> implements Physics, Copyable<EntityPhysics<T>>, Tickable {
    protected final T entity;

    private float lastGravity = DEFAULT_GRAVITY_FACTOR;

    private final DataTracker tracker;
    protected final DataTracker.Entry<Float> gravity;

    public EntityPhysics(T entity) {
        this.entity = entity;
        this.tracker = Trackable.of(entity).getDataTrackers().getPrimaryTracker();
        gravity = tracker.startTracking(TrackableDataType.FLOAT, DEFAULT_GRAVITY_FACTOR);
    }

    @Override
    public void tick() {
        if (entity.level() instanceof ServerLevel sw && isGravityNegative()) {
            if (entity.getY() > entity.level().getHeight() + 64) {
                if (entity instanceof LivingEntity) {
                    entity.hurtServer(sw, entity.damageSources().fellOutOfWorld(), 4F);
                } else {
                    entity.discard();
                }
            }
        }

        float gravity = getGravityModifier();
        if (gravity != lastGravity) {
            lastGravity = gravity;

            onGravitychanged();
        }
    }

    @Override
    public AABB getBoxAtPosition(Vec3 pos, boolean inverted) {
        AABB box = entity.getDimensions(entity.getPose()).makeBoundingBox(pos);
        if (inverted) {
            double yOffset = (pos.y() + 1) + box.maxY;
            box = box.move(0, yOffset, 0);
        }
        return box;
    }

    protected void onGravitychanged() {
        entity.refreshDimensions();

        if (!entity.level().isClientSide() && entity instanceof Mob) {
            ((Mob)entity).getNavigation().stop();
            ((Mob)entity).setTarget(null);
        }
    }

    @Override
    public boolean isFlying() {
        return false;
    }

    @Override
    public Vec3 getMotionAngle() {
        return new Vec3(entity.getViewXRot(1), entity.getViewYRot(1), 0);
    }

    @Override
    public double calcGravity(double worldConstant) {
        return worldConstant * getGravityModifier();
    }

    @Override
    public BlockPos getHeadPosition() {
        BlockPos pos = BlockPos.containing(
                entity.getX(),
                (int)(entity.getY() + 0.5) + entity.getEyeHeight(Pose.STANDING),
                entity.getZ()
        );

        return pos;
    }

    @Override
    public void setBaseGravityModifier(float constant) {
        gravity.set(constant);
    }

    @Override
    public float getBaseGravityModifier() {
        return gravity.get();
    }

    @Override
    public float getPersistantGravityModifier() {
        return getGravityModifier();
    }

    @Override
    public float getGravityModifier() {

        if (entity instanceof LivingEntity) {
            if (((LivingEntity)entity).getAttributes() == null) {
                // may be null due to order of execution in the constructor.
                // Will have the default (1) here in any case, so it's safe to ignore the attribute at this point.
                return getBaseGravityModifier();
            }

            if (((LivingEntity)entity).isSleeping()) {
                return DEFAULT_GRAVITY_FACTOR;
            }

            return getBaseGravityModifier() * (float)((LivingEntity)entity).getAttributeValue(UEntityAttributes.ENTITY_GRAVITY_MODIFIER);
        }

        return getBaseGravityModifier();
    }

    @Override
    public void copyFrom(EntityPhysics<T> other, boolean alive) {
        if (alive) {
            setBaseGravityModifier(other.getBaseGravityModifier());
        }
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        compound.putFloat("gravity", getBaseGravityModifier());
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        setBaseGravityModifier(compound.getFloatOr("gravity", DEFAULT_GRAVITY_FACTOR));
    }

    public void toValue(net.minecraft.world.level.storage.ValueOutput output) {
        output.putFloat("gravity", getBaseGravityModifier());
    }

    public void fromValue(net.minecraft.world.level.storage.ValueInput input) {
        setBaseGravityModifier(input.getFloatOr("gravity", DEFAULT_GRAVITY_FACTOR));
    }

}
