package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.WeaklyOwned;
import com.minelittlepony.unicopia.entity.DynamicLightSource;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;

public class FairyEntity extends PathfinderMob implements DynamicLightSource, WeaklyOwned.Mutable<LivingEntity>, MagicImmune {
    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    private final EntityReference<LivingEntity> assignment = new EntityReference<>();

    private final LightEmitter<?> emitter = new LightEmitter<>(this);

    private Optional<BlockPos> stayingPos = Optional.empty();

    protected FairyEntity(EntityType<FairyEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        FlyingPathNavigation birdNavigation = new FlyingPathNavigation(this, world);
        birdNavigation.setCanOpenDoors(true);
        birdNavigation.setCanFloat(true);
        birdNavigation.setCanOpenDoors(true);
        return birdNavigation;
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(5, new StayGoal());
        goalSelector.addGoal(6, new FollowEntityGoal(2, 2, 30));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 1));
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return USounds.ENTITY_TWITTERMITE_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return USounds.ENTITY_TWITTERMITE_DEATH;
    }

    @Override
    public int getLightLevel() {
        return 15;
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
        return owner;
    }

    @Override
    public Level asWorld() {
        return level();
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public boolean isStaying() {
        return stayingPos.isPresent();
    }

    public void setStaying(BlockPos pos) {
        stayingPos = Optional.ofNullable(pos);
    }

    @Override
    public void tick() {
        setOnGround(true);
        super.tick();
        emitter.tick();

        if (level().getRandom().nextInt(20) == 3) {
            level().addParticle(new MagicParticleEffect(0xFFFFFF), getRandomX(1), getY(), getRandomZ(1), 0, 0, 0);
        }

        if (tickCount % 60 == 0) {
            playSound(USounds.ITEM_MAGIC_AURA, 0.1F, 6);
        }
    }

    @Override
    public void travel(Vec3 movementInput) {
        if (isInWater()) {
            moveRelative(0.02f, movementInput);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.8f));
        } else if (isInLava()) {
            moveRelative(0.02f, movementInput);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.5));
        } else {
            float f = 0.91f;
            if (onGround()) {
                f = level().getBlockState(blockPosition().below()).getBlock().getFriction() * 0.91f;
            }
            float g = 0.16277137f / (f * f * f);
            f = 0.91f;
            if (onGround()) {
                f = level().getBlockState(blockPosition().below()).getBlock().getFriction() * 0.91f;
            }
            moveRelative(onGround() ? 0.1f * g : 0.02f, movementInput);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(f));
        }
        calculateEntityAnimation(false);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {

        if (hand == InteractionHand.MAIN_HAND) {

            if (isStaying()) {
                stayingPos = Optional.empty();
                if (player != getMaster()) {
                    assignment.set(player);
                }
            } else {
                assignment.set(null);
                setStaying(blockPosition());
            }

            playSound(USounds.ENTITY_TWITTERMITE_AMBIENT, getSoundVolume() / 3, getVoicePitch() * 3);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        if (level() instanceof ServerLevel serverWorld) {
            LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(level(), EntitySpawnReason.MOB_SUMMONED);
            lightning.snapTo(getX(), getY(), getZ());
            attacker.thunderHit(serverWorld, lightning);
        }
        gameEvent(GameEvent.LIGHTNING_STRIKE);
        ParticleUtils.spawnParticle(level(), LightningBoltParticleEffect.DEFAULT, position(), Vec3.ZERO);

        return false;
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
        emitter.remove();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.store("owner", net.minecraft.nbt.CompoundTag.CODEC, owner.toNBT(registryAccess()));
        stayingPos.ifPresent(pos -> tag.store("stayingPos", BlockPos.CODEC, pos));
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        owner.fromNBT(tag.read("owner", net.minecraft.nbt.CompoundTag.CODEC).orElseGet(net.minecraft.nbt.CompoundTag::new), registryAccess());
        stayingPos = tag.read("stayingPos", BlockPos.CODEC);
    }

    class FollowEntityGoal extends Goal {
        @Nullable
        private LivingEntity target;

        private final double speed;
        private int updateCountdownTicks;

        private final float minDistance;
        private final float maxDistance;
        private float oldWaterPathFindingPenalty;

        public FollowEntityGoal(double speed, float minDistance, float maxDistance) {
            this.speed = speed;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            target = assignment.get(level());

            if (target == null) {
                target = getMaster();
            }
            if (target == null) {
                target = level().getNearestPlayer(FairyEntity.this, maxDistance);
            }
            return target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && !isStaying() && !getNavigation().isDone() && distanceToSqr(target) > minDistance * minDistance;
        }

        @Override
        public void start() {
            updateCountdownTicks = 0;
            oldWaterPathFindingPenalty = getPathfindingMalus(PathType.WATER);
            setPathfindingMalus(PathType.WATER, 0.0f);
        }

        @Override
        public void stop() {
            target = null;
            getNavigation().stop();
            setPathfindingMalus(PathType.WATER, oldWaterPathFindingPenalty);
        }

        @Override
        public void tick() {
            if (target == null || isLeashed() || isStaying()) {
                return;
            }

            getLookControl().setLookAt(target, 10, getMaxHeadXRot());

            Path currentPath = getNavigation().getPath();
            if (currentPath != null && target.getEyeY() < getY() - 0.5 && level().getBlockState(blockPosition().below(3)).isAir()) {
                push(0, -speed, 0);
            }

            double distance = position().distanceToSqr(target.position());

            double speed = this.speed;

            if (distance > 100) {
                setPos(
                    target.getX() + level().getRandom().nextFloat() / 2F - 0.5F,
                    target.getEyeY(),
                    target.getZ() + level().getRandom().nextFloat() / 2F - 0.5F
                );
                setDeltaMovement(target.getDeltaMovement());
                return;
            } else if (distance > 40) {
                speed *= 10;
            } else if (distance > 25) {
                speed *= 4;
            } else if (distance > 8) {
                speed *= 2;
            }

            getNavigation().setSpeedModifier(speed);

            if (--updateCountdownTicks > 0) {
                return;
            }

            updateCountdownTicks = adjustedTickDelay(10);

            if (distance <= minDistance * minDistance) {

                BlockPos pos = RandomPos.generateRandomDirection(level().getRandom(), 5, 5);
                if (pos != null) {
                    getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), speed);
                } else {
                    getNavigation().stop();
                }
            } else {
                getNavigation().moveTo(target, speed);
            }
        }
    }

    class StayGoal extends Goal {
        private int updateCountdownTicks;

        public StayGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return isStaying();
        }

        @Override
        public boolean canContinueToUse() {
            return isStaying() && !getNavigation().isDone();
        }

        @Override
        public void start() {
            updateCountdownTicks = 0;
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }

        @Override
        public void tick() {
            if (--updateCountdownTicks > 0 || !isStaying()) {
                return;
            }

            updateCountdownTicks = adjustedTickDelay(10);

            stayingPos.ifPresent(pos -> {
                if (pos.distSqr(blockPosition()) <= 1) {
                    getNavigation().stop();
                } else {
                    getNavigation().moveTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, flyDist);
                }
            });
        }
    }
}
