package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.AmuletSelectors;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.ai.ArenaAttackGoal;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleSource;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;

public class SombraEntity extends Monster implements ArenaCombatant, ParticleSource<SombraEntity> {
    static final byte BITE = 70;
    static final int MAX_BITE_TIME = 20;
    static final Predicate<Entity> EFFECT_TARGET_PREDICATE = EntitySelector.NO_CREATIVE_OR_SPECTATOR
            .and(e -> e instanceof Player)
            .and(e -> !(AbstractDisguiseSpell.getAppearance(e) instanceof SombraEntity));

    private static final EntityDataAccessor<Optional<BlockPos>> HOME_POS = SynchedEntityData.defineId(SombraEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Float> TARGET_SIZE = SynchedEntityData.defineId(SombraEntity.class, EntityDataSerializers.FLOAT);

    private final ServerBossEvent bossBar;
    final EntityReference<StormCloudEntity> stormCloud = new EntityReference<>();

    private int prevBiteTime;
    private int biteTime;

    private float prevSize;
    private float currentSize;

    public static void startEncounter(Level world, BlockPos pos) {
        if (world.getEntitiesOfClass(Entity.class, new AABB(pos).inflate(16), e -> {
            return e instanceof SombraEntity || e instanceof StormCloudEntity cloud && cloud.cursed;
        }).size() > 0) {
            return;
        }

        StormCloudEntity cloud = UEntities.STORM_CLOUD.create(world, EntitySpawnReason.EVENT);
        cloud.setPos(Vec3.atCenterOf(pos.above(10)));
        cloud.setSize(1);
        cloud.cursed = true;
        world.addFreshEntity(cloud);
    }

    public SombraEntity(EntityType<SombraEntity> type, Level world) {
        this(type, world, null);
    }

    public SombraEntity(EntityType<SombraEntity> type, Level world, @Nullable ServerBossEvent bossBar) {
        super(type, world);
        this.bossBar = bossBar == null ? createBossBar(getDisplayName()) : bossBar;
        this.bossBar.setName(getDisplayName());
        this.bossBar.setOverlay(BossBarOverlay.NOTCHED_10);
    }

    public static ServerBossEvent createBossBar(Component name) {
        return new SombraBossBar(name);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2000)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.ATTACK_DAMAGE, 22);
    }

    @Override
    public SombraEntity asEntity() {
        return this;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HOME_POS, Optional.empty());
        builder.define(TARGET_SIZE, 1F);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new ArenaAttackGoal<>(this));
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 1.3F));
        goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.5F, 32));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, LivingEntity.class, 32F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 1));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        GroundPathNavigation nav = new GroundPathNavigation(this, world) {
            @Override
            protected PathFinder createPathFinder(int range) {
                nodeEvaluator = new FlyNodeEvaluator();
                nodeEvaluator.setCanPassDoors(true);
                return new PathFinder(nodeEvaluator, range);
            }

            @Override
            protected boolean canMoveDirectly(Vec3 origin, Vec3 target) {
                return FlyingPathNavigation.isClearForMovementBetween(mob, origin, target, true);
            }

            @Override
            protected boolean canUpdatePath() {
                return canFloat() && isInLiquid() || !mob.isPassenger();
            }

            @Override
            protected Vec3 getTempMobPos() {
                return mob.position();
            }

            @Override
            @Nullable
            public Path createPath(BlockPos target, int distance) {
                return createPath(ImmutableSet.of(target), 8, false, distance);
            }
        };
        nav.setCanOpenDoors(true);
        nav.setCanFloat(true);
        nav.setCanOpenDoors(true);
        nav.canCutCorner(PathType.UNPASSABLE_RAIL);
        return nav;
    }

    @Override
    public Optional<BlockPos> getHomePos() {
        return entityData.get(HOME_POS);
    }

    public void setHomePos(BlockPos pos) {
        entityData.set(HOME_POS, Optional.of(pos));
    }

    public void setHomePos(Optional<BlockPos> pos) {
        entityData.set(HOME_POS, pos);
    }

    public float getBiteAmount(float tickDelta) {
        float progress = (Mth.lerpInt(tickDelta, prevBiteTime, biteTime) / (float)MAX_BITE_TIME);
        return 1 - Math.abs(Mth.sin(progress * Mth.PI * 3));
    }

    @Override
    public float getAgeScale() {
        return Math.max(1, entityData.get(TARGET_SIZE));
    }

    public float getScaleFactor(float tickDelta) {
        return Mth.lerp(tickDelta, prevSize, currentSize);
    }

    public void setScaleFactor(float targetSize) {
        entityData.set(TARGET_SIZE, targetSize);
        refreshDimensions();
    }

    @Override
    public void tick() {
        setPersistenceRequired();
        Optional<BlockPos> homePos = getHomePos();

        if (homePos.isEmpty()) {
            setHomePos(level().getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, blockPosition()));
            homePos = getHomePos();
        }

        if (!isRemoved()) {
            if (getNavigation().isDone()) {
                if (blockPosition().distSqr(homePos.get()) > Mth.square(getAreaRadius())) {
                    teleportTo(Vec3.atCenterOf(homePos.get()));
                    getNavigation().stop();
                }
            }

            prevBiteTime = biteTime;
            if (biteTime > 0) {
                biteTime--;
            }

            float targetSize = getAgeScale();
            boolean sizeChanging = prevSize != currentSize;
            prevSize = currentSize;
            tickGrowth(targetSize, sizeChanging);
        }

        super.tick();

        if (getTarget() == null && getDeltaMovement().y < -0.1F) {
            setDeltaMovement(getDeltaMovement().multiply(1, 0.4, 1));
        }

        push(0, 0.0442F, 0);

        LivingEntity target = getTarget();

        if (target != null && target.getY() > getY() && getDeltaMovement().y() < 0.5F) {

            float velocityChange = (float)Mth.clamp((target.getY() - getY()) * 0.05F, -0.3F, 0.3F);

            if (target instanceof WitherBoss || target instanceof net.minecraft.world.entity.Mob mob && mob.getNavigation() instanceof FlyingPathNavigation) {
                target.push(0, -velocityChange * 20, 0);
            } else {
                push(0, velocityChange, 0);
            }
        }

        if (isDeadOrDying()) {
            return;
        }

        if (isUnderWater()) {
            jumpFromGround();
        }

        if (random.nextInt(1200) == 0) {
            laugh();
        }

        if (random.nextInt(340) == 0) {
            playSound(SoundEvents.AMBIENT_CAVE.value(), 1, 0.3F);
        } else if (random.nextInt(1340) == 0) {
            playSound(USounds.ENTITY_SOMBRA_AMBIENT, 1, 1);
        }

        if (level().isClientSide()) {
            generateBodyParticles();
        } else {
            if (((ServerLevel)level()).getGameRules().get(GameRules.MOB_GRIEFING)) {
                for (BlockPos p : BlockPos.betweenClosed(blockPosition().offset(-2, -1, -2), blockPosition().offset(2, 1, 2))) {
                    if (level().getBlockState(p).getLightEmission() > 13) {
                        destroyLightSource(p);
                    }
                }
            }

            float healthPercentage = 100 * (getHealth() / getMaxHealth());
            float difference = Mth.abs(healthPercentage - Mth.floor(healthPercentage));

            if (random.nextInt(healthPercentage < 90 && difference < 0.25F ? 19 : 120) == 0) {
                for (BlockPos p : BlockPos.randomInCube(random, 5, blockPosition(), 20)) {
                    CrystalShardsEntity.infestBlock((ServerLevel)level(), p);
                }
            }

            if (getTarget() == null && getNavigation().isDone()) {
                getNavigation().moveTo(homePos.get().getX(), homePos.get().getY() + 5, homePos.get().getZ(), 2);
            }
        }

        if (getTarget() != null && getTarget().isRemoved()) {
            setTarget(null);
        }

        if (getHealth() < getMaxHealth()) {
            for (Entity shard : level().getEntitiesOfClass(CrystalShardsEntity.class, getBoundingBox().inflate(50), EntitySelector.ENTITY_STILL_ALIVE)) {

                if (tickCount % 150 == 0) {
                    heal(2);

                    ParticleUtils.spawnParticle(level(),
                            new FollowingParticleEffect(UParticles.HEALTH_DRAIN, this, 0.2F)
                            .withChild(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE),
                            shard.position(),
                            Vec3.ZERO
                    );
                }
            }
        }

        getHomePos().ifPresent(this::generateArenaEffects);
    }

    protected void tickGrowth(float targetSize, boolean changing) {
        if (currentSize != targetSize) {
            float sizeDifference = (entityData.get(TARGET_SIZE) - currentSize);
            currentSize = Math.abs(sizeDifference) < 0.01F ? targetSize : currentSize + (sizeDifference * 0.2F);
            refreshDimensions();
        }

        if (currentSize == targetSize && changing) {
            laugh();
        }

        if (currentSize == targetSize && isDeadOrDying()) {
            setScaleFactor(currentSize + 1);
        }
    }

    private void laugh() {
        if (!level().isClientSide()) {
            playSound(USounds.ENTITY_SOMBRA_LAUGH, 1, 1);
            level().broadcastEntityEvent(this, BITE);
        }
    }

    protected void applyAreaEffects(Player target) {
        if (this.tickCount % 150 == 0) {
            target.playSound(
                    random.nextInt(30) == 0 ? USounds.ENTITY_SOMBRA_AMBIENT
                            : random.nextInt(10) == 0 ? USounds.ENTITY_SOMBRA_SCARY
                            : USounds.Vanilla.AMBIENT_CAVE.value(),
                    random.triangle(1, 0.2F),
                    random.triangle(0.3F, 0.2F)
            );
        }

        if (this.tickCount % 1000 < 50) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 26, 0, true, false));
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 26, 0, true, false));
        }

        if (getTarget() == null && target.distanceTo(this) < getAreaRadius() / 2F) {
            if (teleportTo(target.position())) {
                setPos(position().add(0, 4, 0));
            }
        }
    }

    protected void destroyLightSource(BlockPos pos) {
        level().destroyBlock(pos, true);
        playSound(USounds.ENTITY_SOMBRA_SNICKER, 1, 1);
    }

    protected void generateBodyParticles() {
        for (int i = 0; i < 3; i++) {
            level().addParticle(ParticleTypes.LARGE_SMOKE,
                    random.triangle(getX(), 3),
                    random.triangle(getY(), 3),
                    random.triangle(getZ(), 3),
                    0,
                    0,
                    0
                );
        }
    }

    private void generateArenaEffects(BlockPos home) {
        if (level().isClientSide()) {
            Stream.concat(
                    new Sphere(false, getAreaRadius()).translate(home).randomPoints(random).filter(this::isSurfaceBlock).limit(80),
                    new Sphere(true, getAreaRadius()).translate(home).randomPoints(random).filter(this::isSurfaceBlock).limit(30))
                .forEach(pos -> {
                    ParticleOptions type = random.nextInt(3) < 1 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SOUL_FIRE_FLAME;
                    ParticleUtils.spawnParticle(level(), type, pos, Vec3.ZERO);
                    ParticleUtils.spawnParticle(level(), type, pos, pos.subtract(position()).add(0, 0.1, 0).scale(-0.013));
                });
        } else {
            VecHelper.findInRange(this, level(), Vec3.atCenterOf(home), getAreaRadius() - 0.2F, EFFECT_TARGET_PREDICATE).forEach(e -> {
                applyAreaEffects((Player)e);
            });
        }
    }

    private boolean isSurfaceBlock(Vec3 pos) {
        BlockPos bPos = BlockPos.containing(pos);
        return level().isEmptyBlock(bPos) && !level().isEmptyBlock(bPos.below());
    }

    @Override
    protected void customServerAiStep(ServerLevel world) {
        super.customServerAiStep(world);
        bossBar.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d = 64 * getViewScale();
        return distance < d * d;
    }

    @Override
    public boolean causeFallDamage(double distance, float damageMultiplier, DamageSource cause) {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player) {
            if (AmuletSelectors.ALICORN_AMULET.test(player)) {
                if (!level().isClientSide()) {
                    playSound(USounds.ENTITY_SOMBRA_SNICKER, 1, 1);
                    player.sendSystemMessage(Component.translatable("entity.unicopia.sombra.taunt"));
                }
            }
            TrinketsDelegate.EquippedStack amulet = AmuletItem.getForEntity(player, UItems.ALICORN_AMULET);
            if (!amulet.stack().isEmpty()) {
                amulet.stack().shrink(1);
                amulet.markChanged();
            }
        }
        boolean damaged = super.hurtServer(world, source, amount);

        if (!level().isClientSide()) {
            if (source.getEntity() instanceof LivingEntity attacker) {
                teleportRandomly(6);

                if (!(attacker instanceof Player player && (player.isCreative() || player.isSpectator()))) {
                    setTarget(attacker);
                }
            }

            float targetSize = getAgeScale();
            if (targetSize > 1) {
                setScaleFactor(Math.max(1, targetSize * 0.9F));
            }
        }

        return damaged;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!dead) {
            stormCloud.ifPresent(level(), cloud -> {
                cloud.setStormTicks(0);
                cloud.setDissipating(true);
            });
            stormCloud.set(null);
            getHomePos().ifPresent(home -> {
                VecHelper.findInRange(this, level(), Vec3.atCenterOf(home), getAreaRadius() - 0.2F, e -> e instanceof CrystalShardsEntity).forEach(e -> {
                    ((CrystalShardsEntity)e).setDecaying(true);
                });

                VecHelper.findInRange(this, level(), Vec3.atCenterOf(home), getAreaRadius() - 0.2F, EFFECT_TARGET_PREDICATE).forEach(player -> {
                    Pony.of((Player)player).getCorruption().set(0);
                    UCriteria.DEFEAT_SOMBRA.trigger(player);
                });
            });

        }
        super.die(damageSource);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean causedByPlayer) {
        super.dropCustomDeathLoot(world, source, causedByPlayer);
        ItemEntity itemEntity = spawnAtLocation(world, UItems.BROKEN_ALICORN_AMULET);
        if (itemEntity != null) {
            itemEntity.setExtendedLifetime();
        }
    }

    @Override
    protected void tickDeath() {
        if (++deathTime >= 180 && deathTime <= 200) {
            level().addParticle(ParticleTypes.EXPLOSION_EMITTER,
                    random.triangle(getX(), 4F),
                    random.triangle(getY() + 2, 2F),
                    random.triangle(getZ(), 4F), 0, 0, 0);
        }

        move(MoverType.SELF, new Vec3(0, 0.3F, 0));

        if (level() instanceof ServerLevel sw) {
            final boolean dropLoot = sw.getGameRules().get(GameRules.MOB_DROPS);
            final int experience = 500;

            if (deathTime > 150 && deathTime % 5 == 0 && dropLoot) {
                ExperienceOrb.award(sw, position(), Mth.floor(experience * 0.08f));
            }

            if (deathTime == 1 && !isSilent()) {
                level().globalLevelEvent(LevelEvent.SOUND_DRAGON_DEATH, this.blockPosition(), 0);
            }

            if (deathTime == 200) {
                if (dropLoot) {
                    ExperienceOrb.award(sw, position(), Mth.floor(experience * 0.2f));
                }
                remove(RemovalReason.KILLED);
                gameEvent(GameEvent.ENTITY_DIE);
            }
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof SombraEntity
                || EquinePredicates.IS_MAGIC_IMMUNE.test(target)
                || !EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
            return false;
        }

        if (target != null && !target.isRemoved() && target == getTarget()) {
            return true;
        }
        return super.canAttack(target) && getHomePos().filter(home -> target.position().closerThan(Vec3.atCenterOf(home), getAreaRadius())).isPresent();
    }

    public boolean isWithinRestriction(BlockPos pos) {
        BlockPos center = getHomePos().orElse(blockPosition());
        double distance = pos.distToCenterSqr(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5);
        return distance < Mth.square(getAreaRadius());
    }

    @Override
    public boolean doHurtTarget(ServerLevel world, Entity target) {
        laugh();
        return super.doHurtTarget(world, target);
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status == BITE) {
            biteTime = MAX_BITE_TIME;
        } else {
            super.handleEntityEvent(status);
        }
    }

    protected boolean teleportRandomly(int maxDistance) {
        if (level().isClientSide() || !isAlive()) {
            return false;
        }
        return teleportTo(position().add(VecHelper.triangular(random, 0, maxDistance)));
    }

    @Override
    public boolean teleportTo(Vec3 destination) {
        Vec3 oldPos = position();
        if (canTeleportTo(destination) && randomTeleport(destination.x, destination.y, destination.z, true, BlockState::isSolid)) {
            level().gameEvent(GameEvent.TELEPORT, oldPos, GameEvent.Context.of(this));
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean canTeleportTo(Vec3 destination) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(destination.x, destination.y, destination.z);
        while (mutable.getY() > level().getMinY() && !level().getBlockState(mutable).isSolid()) {
            mutable.move(Direction.DOWN);
        }
        BlockState destinationState = level().getBlockState(mutable);
        return destinationState.isSolid() && !destinationState.getFluidState().is(FluidTags.WATER);
    }

    @Override
    @Deprecated
    public float getLightLevelDependentMagicValue() {
        return super.getLightLevelDependentMagicValue() * 0.2F;
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossBar.setName(getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.storeNullable("homePos", BlockPos.CODEC, getHomePos().orElse(null));
        nbt.store("cloud", net.minecraft.nbt.CompoundTag.CODEC, stormCloud.toNBT(registryAccess()));
        nbt.putFloat("size", getAgeScale());
    }

    @Override
    public void readAdditionalSaveData(ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        nbt.read("homePos", BlockPos.CODEC).ifPresent(this::setHomePos);
        if (hasCustomName()) {
            bossBar.setName(getDisplayName());
        }
        setScaleFactor(nbt.getFloatOr("size", 1));
        stormCloud.fromNBT(nbt.read("cloud", net.minecraft.nbt.CompoundTag.CODEC).orElseGet(net.minecraft.nbt.CompoundTag::new), registryAccess());
    }

    private static class SombraBossBar extends ServerBossEvent {
        public SombraBossBar(Component displayName) {
            super(java.util.UUID.randomUUID(), displayName, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
            setDarkenScreen(true);
            setCreateWorldFog(true);
            setPlayBossMusic(true);
        }

        @Override
        public void setProgress(float percent) {
            super.setProgress(percent);
            if (percent > 0.6F && getColor() == BossBarColor.PURPLE) {
                setColor(BossBarColor.RED);
            }
        }
    }
}
