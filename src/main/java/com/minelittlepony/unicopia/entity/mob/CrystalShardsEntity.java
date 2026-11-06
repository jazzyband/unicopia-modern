package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.behaviour.Guest;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.particle.ParticleUtils;

public class CrystalShardsEntity extends StationaryObjectEntity {
    static final byte SHAKE = 1;

    static final int FULL_GROWTH_AGE = 25;

    private static final Set<Direction> ALL_DIRECTIONS = Set.of(Direction.values());
    private static final EntityDataAccessor<Direction> ATTACHMENT_FACE = SynchedEntityData.defineId(CrystalShardsEntity.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Integer> GROWTH = SynchedEntityData.defineId(CrystalShardsEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DECAYING = SynchedEntityData.defineId(CrystalShardsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CORRUPT = SynchedEntityData.defineId(CrystalShardsEntity.class, EntityDataSerializers.BOOLEAN);

    public static boolean infestBlock(ServerLevel world, BlockPos pos) {
        if (world.isEmptyBlock(pos) || !world.getFluidState(pos).is(Fluids.EMPTY)) {
            return false;
        }
        boolean success = false;
        for (Direction face : getFreeFaces(world, pos)) {
            CrystalShardsEntity shards = UEntities.CRYSTAL_SHARDS.create(world, EntitySpawnReason.EVENT);
            shards.setPos(Vec3.atCenterOf(pos.relative(face)));
            shards.setAttachmentFace(face);
            shards.setCorrupt(true);

            world.addFreshEntity(shards);
            success = true;
        }
        return success;
    }

    public static Set<Direction> getFreeFaces(Level world, BlockPos pos) {
        Set<Direction> freeFaces = new HashSet<>(ALL_DIRECTIONS);
        freeFaces.removeAll(getOccupiedFaces(world, pos));
        freeFaces.removeIf(face -> isInvalid(world, pos.relative(face), face));
        return freeFaces;
    }

    public static Set<Direction> getOccupiedFaces(Level world, BlockPos pos) {
        return world.getEntitiesOfClass(CrystalShardsEntity.class, new AABB(pos).inflate(4), EntitySelector.ENTITY_STILL_ALIVE)
                .stream()
                .map(e -> e.getAttachmentFace())
                .distinct()
                .collect(Collectors.toSet());
    }

    static boolean isInvalid(Level world, BlockPos crystalPos, Direction attachmentFace) {
        if (!world.isEmptyBlock(crystalPos) || world.isOutsideBuildHeight(crystalPos)) {
            return true;
        }
        BlockPos attachmentPos = crystalPos.relative(attachmentFace.getOpposite());
        return !world.getBlockState(attachmentPos).isFaceSturdy(world, attachmentPos, attachmentFace, SupportType.RIGID);
    }

    private int prevAge;

    private int ticksShaking;

    public CrystalShardsEntity(EntityType<CrystalShardsEntity> type, Level world) {
        super(type, world);
        setYRot(world.getRandom().nextFloat() * 360);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACHMENT_FACE, Direction.UP);
        builder.define(GROWTH, 0);
        builder.define(DECAYING, false);
        builder.define(CORRUPT, false);
    }

    @Override
    public float getMaxHealth() {
        return 15F;
    }

    public float getGrowth(float tickDelta) {
        int age = getGrowth();
        float lerped = Mth.clamp(Mth.lerpInt(tickDelta, prevAge, age), 0, FULL_GROWTH_AGE) / (float)FULL_GROWTH_AGE;
        return lerped * 2 * (1 + Math.abs(getUUID().getLeastSignificantBits() % 20) / 20F);
    }

    public int getGrowth() {
        return entityData.get(GROWTH);
    }

    public void setGrowth(int growth) {
        entityData.set(GROWTH, Math.max(0, growth));
    }

    public void setDecaying(boolean decaying) {
        entityData.set(DECAYING, decaying);
    }

    public boolean isDecaying() {
        return entityData.get(DECAYING);
    }
    public void setCorrupt(boolean corrupted) {
        entityData.set(CORRUPT, corrupted);
    }

    public boolean isCorrupt() {
        return entityData.get(CORRUPT);
    }

    public boolean isShaking() {
        return ticksShaking > 0;
    }

    public Direction getAttachmentFace() {
        return Objects.requireNonNullElse(entityData.get(ATTACHMENT_FACE), Direction.UP);
    }

    public void setAttachmentFace(@Nullable Direction face) {
        entityData.set(ATTACHMENT_FACE, face == null ? Direction.UP : face);
    }

    @Override
    public void tick() {
        prevAge = getGrowth();
        if (!level().isClientSide()) {
            int growAmount = 1 + Math.abs((int)getUUID().getLeastSignificantBits() % 5);
            setGrowth(prevAge + (isDecaying() ? -growAmount : growAmount));
        }
        setRemainingFireTicks(0);

        if (isDecaying() && getGrowth() == 0) {
            discard();
        }

        if (ticksShaking > 0) {
            ticksShaking--;
        }

        setPos(Vec3.atCenterOf(blockPosition()));

        super.tick();

        if (ticksShaking > 0 || getGrowth() < FULL_GROWTH_AGE) {
            if (tickCount % random.nextIntBetweenInclusive(2, 5) == 0) {
                playSound(USounds.ENTITY_CRYSTAL_SHARDS_AMBIENT, 1,
                        1 - Mth.clamp(getGrowth(1), 0, 1) * 0.5F);
            }
        }

        if (!Guest.of(this).hasHost()) {
            if (isDead() || isInvalid(level(), blockPosition(), getAttachmentFace())) {
                if (level() instanceof ServerLevel sw) {
                    kill(sw);
                }
                ParticleUtils.spawnParticles(ParticleTypes.CLOUD, this, 10);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        onHurt();
        setHealth(Math.max(0, getHealth() - amount));
        if (getHealth() <= 0) {
            kill(world);
        }
        return true;
    }

    @Override
    protected void onHurt() {
        level().broadcastEntityEvent(this, SHAKE);
        ticksShaking = 10;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.KILLED) {
            playSound(USounds.ENTITY_CRYSTAL_SHARDS_JOSTLE, 1, 1);
            if (level() instanceof ServerLevel sw) {
                spawnAtLocation(sw, new ItemStack(UItems.CRYSTAL_SHARD, 6));
            }
        }
        super.remove(reason);
    }

    @Override
    public void handleEntityEvent(byte status) {
        switch (status) {
            case SHAKE:
                ticksShaking = 10;
                break;
            default:
                super.handleEntityEvent(status);
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("yaw", getYRot());
        nbt.putInt("growth", getGrowth());
        nbt.storeNullable("face", Direction.CODEC, getAttachmentFace());
        nbt.putBoolean("decaying", isDecaying());
        nbt.putBoolean("corrupt", isCorrupt());
    }

    @Override
    public void readAdditionalSaveData(ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        setYRot(nbt.getFloatOr("yaw", getYRot()));
        setGrowth(nbt.getIntOr("growth", 0));
        setAttachmentFace(nbt.read("face", Direction.CODEC).orElse(null));
        setDecaying(nbt.getBooleanOr("decaying", false));
        setCorrupt(nbt.getBooleanOr("corrupt", false));
        prevAge = getGrowth();
    }
}
