package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.WeaklyOwned;
import com.minelittlepony.unicopia.ability.magic.*;
import com.minelittlepony.unicopia.ability.magic.spell.effect.TargetSelecter;
import com.minelittlepony.unicopia.entity.ai.BreakHeartGoal;
import com.minelittlepony.unicopia.entity.ai.DynamicTargetGoal;
import com.minelittlepony.unicopia.entity.ai.EatMuffinGoal;
import com.minelittlepony.unicopia.entity.ai.FleeExplosionGoal;
import com.minelittlepony.unicopia.entity.ai.PredicatedGoal;
import com.minelittlepony.unicopia.entity.ai.PrioritizedActiveTargetGoal;
import com.minelittlepony.unicopia.entity.ai.TargettingUtil;
import com.minelittlepony.unicopia.entity.ai.WantItTakeItGoal;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.HolderLookup;

public class Creature extends Living<LivingEntity> implements WeaklyOwned.Mutable<LivingEntity> {
    public static void boostrap() {}

    private final EntityPhysics<LivingEntity> physics;

    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    private Optional<GoalSelector> goals = Optional.empty();

    private int eatTimer;
    @Nullable
    private EatMuffinGoal eatMuffinGoal;

    private int smittenTicks;

    private final Predicate<LivingEntity> targetPredicate = TargetSelecter.<LivingEntity>validTarget(() -> getOriginatingCaster().getAffinity(), this).and(e -> {
        return Equine.of(e)
                .filter(eq -> eq instanceof Creature)
                .filter(eq -> isDiscorded() != ((Creature)eq).hasCommonOwner(this))
                .isEmpty();
    });

    protected final DataTracker.Entry<Integer> eating;
    protected final DataTracker.Entry<Boolean> discorded;
    protected final DataTracker.Entry<Boolean> smitten;

    public Creature(LivingEntity entity) {
        super(entity);
        physics = new EntityPhysics<>(entity);
        addTicker(physics);
        addTicker(this::updateConsumption);

        tracker.startTracking(owner);
        eating = tracker.startTracking(TrackableDataType.INT, 0);
        discorded = tracker.startTracking(TrackableDataType.BOOLEAN, false);
        smitten = tracker.startTracking(TrackableDataType.BOOLEAN, false);
    }

    @Override
    public void setMaster(LivingEntity owner) {
        this.owner.set(owner);
    }

    public boolean isMinion() {
        return getMasterReference().isSet();
    }

    public boolean isDiscorded() {
        return discorded.get();
    }

    public boolean isSmitten() {
        return smitten.get();
    }

    public void setSmitten(boolean smitten) {
        smittenTicks = smitten ? 20 : 0;
        this.smitten.set(smitten);
    }

    public void setDiscorded(boolean discorded) {
        this.discorded.set(discorded);
    }

    @Override
    @NotNull
    public LivingEntity getMaster() {
        return getMasterReference().getOrEmpty(asWorld()).orElse(entity);
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
        return owner;
    }

    public Optional<GoalSelector> getGoals() {
        return goals;
    }

    public void initAi(GoalSelector goals, GoalSelector targets) {
        this.goals = Optional.of(goals);

        if (entity instanceof MagicImmune) {
            return;
        }

        DynamicTargetGoal targetter = new DynamicTargetGoal((Mob)entity);
        targets.addGoal(1, targetter);
        if (!InteractionManager.getInstance().getSyncedConfig().wantItNeedItExcludeList().contains(EntityType.getKey(entity.getType()).toString())) {
            goals.addGoal(1, new WantItTakeItGoal(this, targetter));
        }
        if (entity.getType().getCategory() == MobCategory.MONSTER) {
            goals.addGoal(3, new BreakHeartGoal((Mob)entity, targetter));
            if (entity instanceof AbstractSkeleton) {
                targets.addGoal(1, new PrioritizedActiveTargetGoal<>((Mob)entity, Player.class, TargettingUtil.FLYING_PREFERRED, true));
            }
        }
        if (entity instanceof Pig pig) {
            eatMuffinGoal = new EatMuffinGoal(pig, targetter);
            goals.addGoal(3, eatMuffinGoal);
        }
        if (entity instanceof TamableAnimal tameable) {
            goals.addGoal(3, new FleeExplosionGoal(tameable, 6, 1, 1.2));
        }

        if (entity instanceof Creeper mob) {
            goals.addGoal(1, new AvoidEntityGoal<>(mob, LivingEntity.class, 10, 1.5, 1.9, AmuletSelectors.ALICORN_AMULET));
        }
        if (entity instanceof AgeableMob mob) {
            goals.addGoal(1, new AvoidEntityGoal<>(mob, LivingEntity.class, 10, 1.1, 1.7, AmuletSelectors.ALICORN_AMULET_AFTER_1_DAYS));
        }

        final BooleanSupplier isMinion = () -> getMasterReference().isSet();
        final BooleanSupplier isNotMinion = () -> !isMinion.getAsBoolean();

        PredicatedGoal.applyToAll(targets, isNotMinion);
        targets.addGoal(2, new PredicatedGoal(new ActiveEnemyGoal<>(Player.class), isMinion));
        targets.addGoal(2, new PredicatedGoal(new ActiveEnemyGoal<>(Monster.class), isMinion));
        targets.addGoal(2, new PredicatedGoal(new ActiveEnemyGoal<>(Slime.class), isMinion));

        if (entity instanceof Mob mob) {
            final BooleanSupplier isDiscorded = () -> isNotMinion.getAsBoolean() && isDiscorded();
            PredicatedGoal.applyToAll(goals, () -> !isDiscorded.getAsBoolean());

            goals.addGoal(1, new PredicatedGoal(new FloatGoal(mob), isDiscorded));
            if (mob instanceof PathfinderMob pae) {
                goals.addGoal(5, new PredicatedGoal(new WaterAvoidingRandomStrollGoal(pae, 0.8), isDiscorded));
            }
            goals.addGoal(6, new PredicatedGoal(new LookAtPlayerGoal(mob, Player.class, 8.0f), isDiscorded));
            goals.addGoal(6, new PredicatedGoal(new RandomLookAroundGoal(mob), isDiscorded));
        }
    }

    public static void registerAttributes(AttributeSupplier.Builder builder) {
        builder.add(Attributes.ATTACK_DAMAGE);
        builder.add(Attributes.ATTACK_KNOCKBACK);
        builder.add(UEntityAttributes.ENTITY_GRAVITY_MODIFIER);
    }

    @Override
    public boolean beforeUpdate() {
        if (isDiscorded()) {
            spawnParticles(ParticleTypes.ELECTRIC_SPARK, 1);
        }

        if (!isClient() && smittenTicks > 0) {
            if (--smittenTicks <= 0) {
                setSmitten(false);
            }
        }

        return super.beforeUpdate();
    }

    private void updateConsumption() {
        if (isClient()) {
            eatTimer = eating.get();
        } else if (eatMuffinGoal != null) {
            eatTimer = eatMuffinGoal.getTimer();
            eating.set(eatTimer);
        }
    }

    public float getNeckAngle(float delta) {
        if (eatTimer <= 0) {
            return 0;
        }
        if (eatTimer >= 4 && eatTimer <= 36) {
            return 1;
        }
        if (eatTimer < 4) {
            return (eatTimer - delta) / 4F;
        }
        return -(eatTimer - 40 - delta) / 4F;
    }

    public float getHeadAngle(float delta) {
        if (eatTimer > 4 && eatTimer <= 36) {
            float f = (eatTimer - 4 - delta) / 32F;
            return 0.62831855f + 0.21991149f * Mth.sin(f * 28.7F);
        }
        if (eatTimer > 0) {
            return 0.62831855f;
        }
        return entity.getXRot() * ((float)Math.PI / 180);
    }

    @Override
    public Race getSpecies() {
        return Race.HUMAN;
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public void setSpecies(Race race) {
    }

    @Override
    public LevelStore getLevel() {
        return Levelled.ZERO;
    }

    @Override
    public LevelStore getCorruption() {
        return Levelled.ZERO;
    }

    @Override
    public boolean subtractEnergyCost(double amount) {
        if (!isClient()) {
            getMaster().hurtServer(asServerWorld(), asEntity().damageSources().magic(), (int)amount/2);
        }
        return getMaster().getHealth() > 0;
    }

    @Override
    public Affinity getAffinity() {
        if (getMaster() instanceof Affine) {
            Affinity affinity = ((Affine)getMaster()).getAffinity();
            if (isDiscorded()) {
                return affinity == Affinity.BAD ? Affinity.GOOD : affinity == Affinity.GOOD ? Affinity.BAD : affinity;
            }
            return affinity;
        }
        return Affinity.NEUTRAL;
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.put("master", getMasterReference().toNBT(lookup));
        physics.toNBT(compound, lookup);
        compound.putBoolean("discorded", isDiscorded());
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        compound.getCompound("master").ifPresent(c -> getMasterReference().fromNBT(c, lookup));
        physics.fromNBT(compound, lookup);
        setDiscorded(compound.getBooleanOr("discorded", false));
    }

    private class ActiveEnemyGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        public ActiveEnemyGoal(Class<T> targetClass) {
            super((Mob)entity, targetClass, true, (e, w) -> Creature.this.targetPredicate.test(e));
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null  && targetConditions.test(getServerLevel(mob.level()), mob, target) && super.canContinueToUse();
        }
    }
}
