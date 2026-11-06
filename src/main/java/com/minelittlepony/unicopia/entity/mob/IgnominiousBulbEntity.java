package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class IgnominiousBulbEntity extends Mob {
    private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(IgnominiousBulbEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(IgnominiousBulbEntity.class, EntityDataSerializers.INT);
    private static final int BABY_AGE = AgeableMob.BABY_START_AGE;
    private static final int HAPPY_TICKS = 40;
    private static final List<BlockPos> TENTACLE_OFFSETS = List.of(
        new BlockPos(-3, 0, -3), new BlockPos(0, 0, -4), new BlockPos(3, 0, -3),
        new BlockPos(-4, 0,  0),                         new BlockPos(4, 0,  0),
        new BlockPos(-3, 0,  3), new BlockPos(0, 0,  4), new BlockPos(3, 0,  4)
    );
    private static final Codec<Map<BlockPos, EntityReference<TentacleEntity>>> TENTACLES_CODEC = CodecUtils.mapOf(CodecUtils.mapEntryOf(
        BlockPos.CODEC.fieldOf("pos"), EntityReference.<TentacleEntity>codec().fieldOf("target")
    ));

    @Nullable
    private Map<BlockPos, EntityReference<TentacleEntity>> tentacles;

    private int prevAge;
    private int happyTicks;
    private int angryTicks;

    public IgnominiousBulbEntity(EntityType<? extends IgnominiousBulbEntity> type, Level world) {
        super(type, world);
    }

    public IgnominiousBulbEntity(Level world) {
        super(UEntities.IGNOMINIOUS_BULB, world);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGRY, false);
        builder.define(AGE, 0);
    }

    @Override
    public boolean isBaby() {
        return getAge() < 0;
    }

    @Override
    public void setBaby(boolean baby) {
        setAge(BABY_AGE);
    }

    protected int getAge() {
        return entityData.get(AGE);
    }

    protected void setAge(int age) {
        entityData.set(AGE, age);
    }

    public float getScale(float tickDelta) {
        return Math.max(0.2F, 1 - (Mth.clamp(Mth.lerpInt(tickDelta, prevAge, getAge()), BABY_AGE, 0F) / BABY_AGE));
    }

    @Override
    public float getAgeScale() {
        return getScale(1);
    }

    public boolean isAngry() {
        return entityData.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        if (angry != isAngry()) {
            entityData.set(ANGRY, angry);
        }
    }

    public void setAngryFor(int angryTicks) {
        this.angryTicks = angryTicks;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (isBaby() && stack.is(Items.BONE_MEAL)) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            growUp(10);
            if (!level().isClientSide()) {
                level().levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPosition(), 0);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void onOffspringSpawnedFromEgg(Player player, Mob child) {
        Vec3 center = position();
        Supplier<Vec3> offset = VecHelper.supplier(() -> (level().getRandom().nextBoolean() ? 1 : -1) * 3);
        setPos(center.add(offset.get().multiply(1, 0, 1)));
        child.setPos(center.add(offset.get().multiply(1, 0, 1)));
    }

    @Override
    public void setPos(double x, double y, double z) {
        Vec3 change = new Vec3(x, y, z).subtract(position());
        super.setPos(x, y, z);
        getTentacles().values().forEach(tentacle -> {
            tentacle.ifPresent(level(), t -> {
                t.setPos(t.position().add(change));
            });
        });
    }

    public void growUp(int age) {
        int currentAge = getAge();
        if (currentAge < 0) {
            setAge((age * 20) + currentAge);
            happyTicks = HAPPY_TICKS;
        }
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && !isRemoved()) {
            var center = new BlockPos.MutableBlockPos();
            var tentacles = getTentacles();

            if (!isBaby() && !firstTick) {
                TENTACLE_OFFSETS.forEach(offset -> {
                    tentacles.compute(adjustForTerrain(center, offset), this::updateTentacle);
                });
            }

            for (EntityReference<TentacleEntity> tentacle : tentacles.values()) {
                if (level().getRandom().nextInt(isAngry() ? 12 : 1200) == 0) {
                    tentacle.ifPresent(level(), t -> {
                        t.addActiveTicks(120);
                    });
                }
            }

            @Nullable
            LivingEntity target = getLastHurtByMob();
            if (target == null || !canAttack(target)) {
                target = null;
                setLastHurtByMob(null);
            }

            if (angryTicks > 0) {
                angryTicks--;
            }

            setAngry(!isBaby() && (angryTicks > 0 || target != null));

            float healthPercentage = getHealth() / getMaxHealth();
            if (isAngry() && target != null && level().getRandom().nextInt(1 + (int)(healthPercentage * 30)) == 0) {
                if (target instanceof Player player) {
                    Pony.of(player).getMagicalReserves().getEnergy().add(6);
                }

                final LivingEntity t = target;
                tentacles.values()
                        .stream()
                        .flatMap(tentacle -> tentacle.getOrEmpty(level()).stream())
                        .sorted(Comparator.comparing(a -> a.distanceTo(t)))
                        .limit(2)
                        .forEach(tentacle -> {
                    tentacle.setTarget(t);
                });
            }

            if (target != null) {
                lookAt(target, 10, 10);
            }
        }

        super.tick();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        prevAge = getAge();

        if (level().isClientSide()) {
            if (happyTicks > 0 && --happyTicks % 4 == 0) {
                level().addParticle(ParticleTypes.HAPPY_VILLAGER, getRandomX(1), getRandomY() + 0.5, getRandomZ(1), 0, 0, 0);
            }
        } else {
            if (prevAge < 0) {
                setAge(prevAge + 1);
            } else if (prevAge > 0) {
                setAge(prevAge - 1);
            }
        }
    }

    private Map<BlockPos, EntityReference<TentacleEntity>> getTentacles() {
        if (tentacles == null) {
            tentacles = level().getEntitiesOfClass(TentacleEntity.class, getBoundingBox().inflate(5, 0, 5), EntitySelector.ENTITY_STILL_ALIVE)
                    .stream()
                    .collect(Collectors.toMap(TentacleEntity::blockPosition, tentacle -> {
                        tentacle.setBulb(this);
                        return new EntityReference<>(tentacle);
                    }));
        }
        return tentacles;
    }

    private EntityReference<TentacleEntity> updateTentacle(BlockPos pos, @Nullable EntityReference<TentacleEntity> tentacle) {
        if (tentacle == null || tentacle.getOrEmpty(level()).filter(Entity::isAlive).isEmpty()) {
            var created = new TentacleEntity(level(), pos);
            created.absSnapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level().getRandom().nextFloat() * 360, 0);
            created.setBulb(this);

            if (level().loadedAndEntityCanStandOn(pos.below(), this)) {
                level().addFreshEntity(created);
            }

            return new EntityReference<>(created);
        }
        return tentacle;
    }

    private BlockPos adjustForTerrain(BlockPos.MutableBlockPos mutable, BlockPos offset) {
        Level w = level();
        mutable.set(blockPosition());
        mutable.move(offset);
        while (isSpace(w, mutable.below()) && w.isInWorldBounds(mutable)) {
            mutable.move(Direction.DOWN);
        }
        while (!isPosValid(w, mutable) && w.isInWorldBounds(mutable)) {
            mutable.move(Direction.UP);
        }
        if (w.getBlockState(mutable).canBeReplaced()) {
            w.destroyBlock(mutable, true);
        }
        return mutable.immutable();
    }

    private boolean isPosValid(Level w, BlockPos pos) {
        return w.loadedAndEntityCanStandOn(pos.below(), this) && isSpace(w, pos);
    }

    private boolean isSpace(Level w, BlockPos pos) {
        BlockState state = w.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        getTentacles().values().forEach(tentacle -> {
            tentacle.ifPresent(level(), t -> t.remove(reason));
        });
    }

    @Override
    public void move(MoverType movementType, Vec3 movement) { }

    @Override
    public void push(double deltaX, double deltaY, double deltaZ) { }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        if (isBaby()) {
            return SoundEvents.HORSE_BREATHE;
        }
        return USounds.ENTITY_IGNIMEOUS_BULB_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return USounds.ENTITY_IGNIMEOUS_BULB_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (!isBaby() && level().getRandom().nextInt(2) == 0) {
            return SoundEvents.CAMEL_AMBIENT;
        }
        return SoundEvents.BONE_MEAL_USE;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("angry", isAngry());
        nbt.putInt("age", getAge());
        nbt.store("tentacles", TENTACLES_CODEC, getTentacles());
    }

    @Override
    public void readAdditionalSaveData(ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        setAngry(nbt.getBooleanOr("angry", false));
        setAge(nbt.getIntOr("age", 0));
        if (!level().isClientSide()) {
            tentacles = nbt.read("tentacles", TENTACLES_CODEC).map(HashMap::new).orElseGet(null);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (AGE.equals(data)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(data);
    }
}
