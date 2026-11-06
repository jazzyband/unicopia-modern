package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.ai.FleeExplosionGoal;
import com.minelittlepony.unicopia.entity.behaviour.Guest;

public class FriendlyCreeperEntity extends TamableAnimal implements NeutralMob {
    private static final EntityDataAccessor<Integer> FUSE_SPEED = SynchedEntityData.defineId(FriendlyCreeperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CHARGED = SynchedEntityData.defineId(FriendlyCreeperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IGNITED = SynchedEntityData.defineId(FriendlyCreeperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final UniformInt ANGER_TIME_RANGE = TimeUtil.rangeOfSeconds(20, 39);

    private int lastFuseTime;
    private int currentFuseTime;
    private short fuseTime = 30;
    private byte explosionRadius = 3;
    private int headsDropped;
    private short hugTime;
    private short lastHugTime;

    @Nullable
    private net.minecraft.world.entity.EntityReference<LivingEntity> angerTarget;
    private long angerEndTime;

    protected FriendlyCreeperEntity(EntityType<? extends FriendlyCreeperEntity> type, Level world) {
        super(type, world);
        setTame(false, true);
        setPathfindingMalus(PathType.POWDER_SNOW, -1);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FUSE_SPEED, -1);
        builder.define(CHARGED, false);
        builder.define(IGNITED, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(1, new EscapeGoal(1.5));
        goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(3, new IgniteGoal());
        goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Ocelot.class, 6, 1, 1.2));
        goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Cat.class, 6, 1, 1.2));
        goalSelector.addGoal(5, new MeleeAttackGoal(this, 1, false));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers(Wolf.class));
        targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
        targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public static AttributeSupplier.Builder createCreeperAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected Component getTypeName() {
        return EntityTypes.CREEPER.getDescription();
    }

    @Override
    public int getMaxFallDistance() {
        return 3 + (getTarget() == null ? 0 : ((int)(getHealth() - 1)));
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        boolean bl = super.causeFallDamage(fallDistance, damageMultiplier, damageSource);
        currentFuseTime += (int)(fallDistance * 1.5f);
        if (currentFuseTime > fuseTime - 5) {
            currentFuseTime = fuseTime - 5;
        }
        return bl;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        if (entityData.get(CHARGED).booleanValue()) {
            nbt.putBoolean("powered", true);
        }
        nbt.putShort("Fuse", fuseTime);
        nbt.putShort("Hugged", hugTime);
        nbt.putByte("ExplosionRadius", explosionRadius);
        nbt.putBoolean("ignited", isIgnited());
        addPersistentAngerSaveData(nbt);
    }

    @Override
    public void readAdditionalSaveData(ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        entityData.set(CHARGED, nbt.getBooleanOr("powered", false));
        fuseTime = (short)nbt.getShortOr("Fuse", fuseTime);
        hugTime = (short)nbt.getShortOr("Hugged", hugTime);
        explosionRadius = nbt.getByteOr("ExplosionRadius", explosionRadius);
        if (nbt.getBooleanOr("ignited", false)) {
            ignite();
        }
        readPersistentAngerSaveData(level(), nbt);
    }

    @Override
    public void tick() {
        setOrderedToSit(isInSittingPose());

        if (isAlive()) {
            lastFuseTime = currentFuseTime;
            if (isIgnited()) {
                setFuseSpeed(1);
            }
            int fuseSpeed = getFuseSpeed();
            if (fuseSpeed > 0 && currentFuseTime == 0) {
                playSound(SoundEvents.CREEPER_PRIMED, 1.0f, 0.5f);
                gameEvent(GameEvent.PRIME_FUSE);
            }
            currentFuseTime = Math.max(0, currentFuseTime + fuseSpeed);
            if (currentFuseTime >= fuseTime) {
                currentFuseTime = fuseTime;
                explode();
            }

            lastHugTime = hugTime;

            if (!isTame() && ((Guest)this).getHost() == null) {
                if (isConverting()) {
                    if (++hugTime >= 100) {
                        if (!level().isClientSide()) {
                            setOwner(getCreature().getCarrierId().map(level()::getPlayerByUUID).orElse(null));
                            level().broadcastEntityEvent(this, EntityEvent.TAMING_SUCCEEDED);
                        }
                    }

                    if (hugTime % 5 == 0) {
                        playHurtSound(damageSources().generic());
                    }
                } else {
                    hugTime = 0;
                    if (!level().isClientSide()) {
                        convertTo(EntityTypes.CREEPER, ConversionParams.single(this, true, true), entity -> {
                            discard();
                            level().addFreshEntity(entity);
                        });
                    }
                }
            } else {
                if (random.nextInt(30) == 0) {
                    spawnHeart();
                }
                hugTime = 0;
            }
        }

        super.tick();
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!level().isClientSide()) {
            updatePersistentAnger((ServerLevel)level(), true);
        }
    }

    private void spawnHeart() {
        level().addParticle(ParticleTypes.HEART, random.triangle(getX(), 0.5), getY() + getBbHeight(), random.triangle(getZ(), 0.5), 0, 0, 0);
    }

    private Creature getCreature() {
        return Equine.<FriendlyCreeperEntity, Creature>of(this, c -> c instanceof Creature).get();
    }

    public boolean isConverting() {
        return !isTame() && getCreature().isBeingCarried();
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof Goat) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean causedByPlayer) {
        super.dropCustomDeathLoot(world, source, causedByPlayer);
        if (source.getEntity() instanceof Creeper c && c.isPowered()) {
            spawnAtLocation(world, Items.CREEPER_HEAD);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel world, Entity target) {
        return true;
    }

    public boolean isCharged() {
        return entityData.get(CHARGED);
    }

    public float getClientFuseTime(float timeDelta) {
        return Mth.lerp(timeDelta, (float)lastFuseTime, (float)currentFuseTime) / (fuseTime - 2)
             + Mth.lerp(timeDelta, (float)lastHugTime, (float)hugTime) / 98F;
    }

    public int getFuseSpeed() {
        return entityData.get(FUSE_SPEED);
    }

    public void setFuseSpeed(int fuseSpeed) {
        entityData.set(FUSE_SPEED, fuseSpeed);
    }

    @Override
    public void thunderHit(ServerLevel world, LightningBolt lightning) {
        super.thunderHit(world, lightning);
        entityData.set(CHARGED, true);
    }


    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.GUNPOWDER);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.isEmpty() && isOwnedBy(player)) {
            setOrderedToSit(!isOrderedToSit());
            setInSittingPose(isOrderedToSit());
            return InteractionResult.SUCCESS;
        }

        if (stack.is(ItemTags.CREEPER_IGNITERS)) {
            SoundEvent soundEvent = stack.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE;
            level().playSound(player, getX(), getY(), getZ(), soundEvent, getSoundSource(), 1, random.nextFloat() * 0.4f + 0.8f);

            if (!level().isClientSide()) {
                ignite();
                if (!stack.isDamageableItem()) {
                    stack.shrink(1);
                } else {
                    stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                }
            }

            return InteractionResult.SUCCESS;
        }

        if (stack.is(Items.GUNPOWDER) && getHealth() < getMaxHealth()) {
            level().playSound(player, getX(), getY(), getZ(), SoundEvents.GENERIC_EAT.value(), getSoundSource(), 1, random.nextFloat() * 0.4f + 0.8f);

            currentFuseTime = fuseTime - 1;
            if (!level().isClientSide()) {
                heal(3);
                level().broadcastEntityEvent(this, EntityEvent.TAMING_SUCCEEDED);
                if (!stack.isDamageableItem()) {
                    stack.shrink(1);
                } else {
                    stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                }
            }

            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private void explode() {
        if (!level().isClientSide()) {
            dead = true;
            level().explode(this, getX(), getY(), getZ(), getExplosionRadius(), Level.ExplosionInteraction.MOB);
            discard();
            spawnEffectsCloud();
        }
    }

    @Override
    public boolean shouldBlockExplode(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float explosionPower) {
        return false;
    }

    private float getExplosionRadius() {
        return explosionRadius * (isCharged() ? 2 : 1);
    }

    private void spawnEffectsCloud() {
        Collection<MobEffectInstance> effects = getActiveEffects();
        if (!effects.isEmpty()) {
            AreaEffectCloud cloud = new AreaEffectCloud(level(), getX(), getY(), getZ());
            cloud.setRadius(2.5f);
            cloud.setRadiusOnUse(-0.5f);
            cloud.setWaitTime(10);
            cloud.setDuration(cloud.getDuration() / 2);
            cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
            effects.forEach(effect -> cloud.addEffect(new MobEffectInstance(effect)));
            level().addFreshEntity(cloud);
        }
    }

    public boolean isIgnited() {
        return entityData.get(IGNITED);
    }

    public void ignite() {
        entityData.set(IGNITED, true);
        FleeExplosionGoal.notifySurroundings(this, getExplosionRadius());
    }

    public boolean shouldDropHead() {
        return isCharged() && headsDropped < 1;
    }

    public void onHeadDropped() {
        headsDropped++;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob partner) {
        FriendlyCreeperEntity child = (FriendlyCreeperEntity)getType().create(world, EntitySpawnReason.BREEDING);
        var owner = getOwnerReference();
        if (owner != null) {
            child.setOwnerReference(owner);
            child.setTame(true, true);
        }
        return child;
    }

    @Override
    public long getPersistentAngerEndTime() {
        return angerEndTime;
    }

    @Override
    public void setPersistentAngerEndTime(long time) {
        angerEndTime = time;
    }

    @Nullable
    @Override
    public net.minecraft.world.entity.EntityReference<LivingEntity> getPersistentAngerTarget() {
        return angerTarget;
    }

    @Override
    public void setPersistentAngerTarget(net.minecraft.world.entity.EntityReference<LivingEntity> target) {
        angerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        setTimeToRemainAngry(ANGER_TIME_RANGE.sample(random));
    }

    class IgniteGoal extends Goal {
        @Nullable
        private LivingEntity target;

        public IgniteGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = getTarget();
            return getFuseSpeed() > 0 || livingEntity != null && distanceToSqr(livingEntity) < 9.0;
        }

        @Override
        public void start() {
            getNavigation().stop();
            target = getTarget();
        }

        @Override
        public void stop() {
            target = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (target == null) {
                setFuseSpeed(-1);
                return;
            }
            if (distanceToSqr(target) > 49.0) {
                setFuseSpeed(-1);
                return;
            }
            if (!getSensing().hasLineOfSight(target)) {
                setFuseSpeed(-1);
                return;
            }
            setFuseSpeed(1);
        }
    }

    class EscapeGoal extends PanicGoal {

        public EscapeGoal(double speed) {
            super(FriendlyCreeperEntity.this, speed);
        }

        @Override
        protected boolean shouldPanic() {
            return mob.isFreezing() || mob.isOnFire();
        }
    }
}
