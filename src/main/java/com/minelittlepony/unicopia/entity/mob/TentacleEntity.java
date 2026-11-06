package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.Comparator;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.shape.Sphere;

public class TentacleEntity extends HangingEntity {
    static final byte ATTACK_STATUS = 54;
    static final int MAX_GROWTH = 25;
    private static final EntityDataAccessor<Integer> GROWTH = SynchedEntityData.defineId(TentacleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MOTION_OFFSET = SynchedEntityData.defineId(TentacleEntity.class, EntityDataSerializers.INT);

    private int prevGrowth;
    private int attackingTicks;
    private float prevAttackingTicks;

    private int ticksActive;
    private int prevMotionOffset;

    public int hurtTime;

    @Nullable
    private LivingEntity target;
    private final Comparator<LivingEntity> targetSorting = Comparator.comparing(this::distanceTo);

    @Nullable
    private IgnominiousBulbEntity bulb;

    public TentacleEntity(EntityType<? extends TentacleEntity> type, Level world) {
        super(type, world);
    }

    public TentacleEntity(Level world, BlockPos pos) {
        super(UEntities.TENTACLE, world, pos);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        builder.define(GROWTH, 0);
        builder.define(MOTION_OFFSET, 0);
    }

    public void setBulb(IgnominiousBulbEntity bulb) {
        this.bulb = bulb;
    }

    public void attack(BlockPos pos) {
        var offset = Vec3.atCenterOf(pos).subtract(Vec3.atCenterOf(blockPosition()));

        double dX = offset.x;
        double dY = offset.y;
        double dZ = offset.z;
        double radius = Math.sqrt(dX * dX + dZ * dZ);

        setXRot(Mth.wrapDegrees((float)(-(Mth.atan2(dY, radius) * Mth.RAD_TO_DEG))));
        setYRot(Mth.wrapDegrees((float)(Mth.atan2(dZ, dX) * Mth.RAD_TO_DEG) - 90));
        level().broadcastEntityEvent(this, ATTACK_STATUS);
        attackingTicks = 30;
        if (bulb != null) {
            bulb.setAngryFor(10);
            bulb.lookAt(Anchor.FEET, Vec3.atCenterOf(pos));
        }
    }

    public float getAttackProgress(float tickDelta) {
        return (30F - Mth.lerp(tickDelta, prevAttackingTicks, attackingTicks)) / 30F;
    }

    public float getGrowth(float tickDelta) {
        return Mth.lerpInt(tickDelta, prevGrowth, getGrowth()) / (float)MAX_GROWTH;
    }

    public int getGrowth() {
        return entityData.get(GROWTH);
    }

    public void setGrowth(int growth) {
        entityData.set(GROWTH, Math.max(0, growth));
    }

    public float getAnimationTimer(float tickDelta) {
        return (tickCount + tickDelta + (getUUID().getMostSignificantBits() % 100)) * 0.00043F
            + Mth.lerpInt(tickDelta, prevMotionOffset, getMotionOffset()) * 0.002F;
    }

    public int getMotionOffset() {
        return entityData.get(MOTION_OFFSET);
    }

    public void setMotionOffset(int motionOffset) {
        entityData.set(MOTION_OFFSET, motionOffset);
    }

    public boolean isAttacking() {
        return attackingTicks > 0;
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player) {
            if (player.getItemInHand(InteractionHand.MAIN_HAND).is(ItemTags.AXES)) {
                kill(world);
                ParticleUtils.spawnParticles(net.minecraft.core.particles.SpellParticleOption.create(ParticleTypes.EFFECT, 0xFFFFFF, 1), this, 10);
            }
            if (level().getRandom().nextInt(5) == 0 && canTarget(player)) {
                setTarget(player);
            }
        }
        addActiveTicks(20 + level().getRandom().nextInt(30));
        playSound(USounds.ENTITY_TENTACLE_ROAR, 5, 1);

        hurtTime = 10;
        level().broadcastEntityEvent(this, EntityEvent.START_ATTACKING);

        gameEvent(GameEvent.RESONATE_15);
        return true;
    }

    public void addActiveTicks(int ticks) {
        ticksActive = Math.min(200, ticksActive + ticks);
    }

    @Override
    public void tick() {
        prevMotionOffset = getMotionOffset();
        int growth = getGrowth();
        prevGrowth = growth;
        super.tick();
        prevAttackingTicks = attackingTicks;

        if (hurtTime > 0) {
            hurtTime--;
        }

        if (isAttacking()) {
            if (--attackingTicks == 12) {
                if (target != null) {
                    if (!canTarget(target)) {
                        target = null;
                    } else {
                        if (level() instanceof ServerLevel sw) {
                            target.hurtServer(sw, damageSources().source(DamageTypes.MOB_ATTACK, this), 15);
                            Vec3 diff = target.position().subtract(position());
                            target.knockback(1, diff.x, diff.z, damageSources().source(DamageTypes.MOB_ATTACK, this), 1);

                            ParticleUtils.spawnParticles(ParticleTypes.CLOUD, target, 10);

                            for (Entity bystander : level().getEntities(target, target.getBoundingBox().inflate(3))) {
                                if (bystander instanceof LivingEntity l) {
                                    diff = l.position().subtract(position());
                                    l.knockback(1, diff.x, diff.z, damageSources().source(DamageTypes.MOB_ATTACK, this), 1);
                                    ParticleUtils.spawnParticles(ParticleTypes.CLOUD, target, 10);
                                }
                            }
                        }
                    }

                    target = null;
                }
            }
        }

        ParticleUtils.spawnParticles(ParticleTypes.ASH, this, 4);
        var sphere = new Sphere(false, 10).translate(position());
        ParticleUtils.spawnParticles(level(), sphere, ParticleTypes.ASH, 4);

        if (!level().isClientSide()) {
            if (growth >= MAX_GROWTH / 2) {
                if (tickCount % 50 == 0) {
                    updateTarget();
                }

                if (target != null && !isAttacking()) {
                    attack(target.blockPosition());
                }
            }

            if (growth < MAX_GROWTH) {
                setGrowth(growth + 1);

                if (growth == 0) {
                    playSound(USounds.ENTITY_TENTACLE_DIG, 1, 1);
                    gameEvent(GameEvent.RESONATE_1);
                }
            }

            if (level().getRandom().nextInt(110) == 0) {
                playSound(USounds.ENTITY_TENTACLE_AMBIENT, 1, 0.3F);
            }

            if (ticksActive > 0) {
                ticksActive--;
                setMotionOffset(getMotionOffset() + ticksActive);
            }
        } else {
            if (growth < MAX_GROWTH && tickCount % 15 == level().getRandom().nextInt(14)) {
                level().addDestroyBlockEffect(blockPosition().below(), level().getBlockState(blockPosition().below()));
            }
        }
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
        playSound(USounds.ENTITY_TENTACLE_ROAR, 5, 1);

        if (target instanceof Player player) {
            Pony.of(player).getMagicalReserves().getEnergy().add(6);
        }
    }

    public LivingEntity getTarget() {
        return target;
    }

    private void updateTarget() {
        if (!canTarget(target)) {
            target = null;
        }

        if (target == null && !isAttacking()) {
            level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(10, 3, 10), this::canTarget)
                .stream()
                .sorted(targetSorting)
                .findFirst()
                .ifPresent(this::setTarget);
        }
    }

    protected boolean canTarget(LivingEntity target) {
        return target != null
            && target.canBeSeenByAnyone()
            && target.canBeSeenAsEnemy()
            && !target.isDiscrete()
            && canSee(target);
    }

    private boolean canSee(Entity entity) {
        return entity.level() == level()
                && distanceTo(entity) <= 128
                && level().clip(new ClipContext(position(), entity.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public void handleEntityEvent(byte status) {
        switch (status) {
            case ATTACK_STATUS:
                attackingTicks = 30;
                break;
            case EntityEvent.START_ATTACKING:
                hurtTime = 10;
                break;
            default:
                super.handleEntityEvent(status);
        }
    }

    @Override
    public void dropItem(ServerLevel world, Entity breaker) {

    }

    @Override
    public void playPlacementSound() {

    }


    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (level().isEmptyBlock(blockPosition()) && level().getBlockState(blockPosition().below()).is(BlockTags.DIRT)) {
            level().setBlockAndUpdate(blockPosition(), UBlocks.CURING_JOKE.defaultBlockState());
        }
    }


    @Override
    public boolean survives() {
        return level().loadedAndEntityCanStandOn(blockPosition().below(), this);
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction side) {
        return AABB.ofSize(Vec3.atCenterOf(pos.below()), 1, 1, 1).expandTowards(0, 2, 0);
    }

    @Override
    public void move(MoverType movementType, Vec3 movement) { }

    @Override
    public void push(double deltaX, double deltaY, double deltaZ) { }

    @Override
    public void addAdditionalSaveData(ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("growth", getGrowth());
        nbt.putInt("motion_offset", getMotionOffset());
    }

    @Override
    public void readAdditionalSaveData(ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        setGrowth(nbt.getIntOr("growth", 0));
        setMotionOffset(nbt.getIntOr("motion_offset", 0));
    }
}
