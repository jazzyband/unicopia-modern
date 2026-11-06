package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.server.world.WeatherConditions;
import com.minelittlepony.unicopia.util.PosHelper;

public class StormCloudEntity extends Entity implements MagicImmune {
    private static final EntityDataAccessor<Integer> CLEAR_TICKS = SynchedEntityData.defineId(StormCloudEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STORM_TICKS = SynchedEntityData.defineId(StormCloudEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TARGET_SIZE = SynchedEntityData.defineId(StormCloudEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DISSIPATING = SynchedEntityData.defineId(StormCloudEntity.class, EntityDataSerializers.BOOLEAN);

    static final float MAX_SIZE = 30;
    static final int CLEAR_TARGET_ALTITUDE = 90;
    static final int STORMY_TARGET_ALTITUDE = 20;

    private float prevSize;
    private float currentSize;

    public boolean cursed;

    private int phase;
    private int nextPhase;

    @Nullable
    private ServerBossEvent bossBar;
    private final Set<ServerPlayer> trackingPlayers = new HashSet<>();

    public StormCloudEntity(EntityType<StormCloudEntity> type, Level world) {
        super(type, world);
        setSize(1 + random.nextInt(4));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STORM_TICKS, 0);
        builder.define(CLEAR_TICKS, 0);
        builder.define(TARGET_SIZE, 1F);
        builder.define(DISSIPATING, false);
    }

    public boolean isStormy() {
        return getStormTicks() != 0;
    }

    public int getStormTicks() {
        return entityData.get(STORM_TICKS);
    }

    public void setStormTicks(int stormTicks) {
        entityData.set(STORM_TICKS, stormTicks);
        setClearTicks(stormTicks);
    }

    public void setDissipating(boolean dissipating) {
        entityData.set(DISSIPATING, dissipating);
    }

    public boolean isDissipating() {
        return entityData.get(DISSIPATING);
    }

    private void setClearTicks(int clearTicks) {
        entityData.set(CLEAR_TICKS, clearTicks);
    }

    private int getClearTicks() {
        return entityData.get(CLEAR_TICKS);
    }

    public boolean tickClear() {
        int clearTicks = getClearTicks();
        if (clearTicks > 0) {
            setClearTicks(--clearTicks);
        }
        return clearTicks > 0;
    }

    public float getSize(float tickDelta) {
        return Mth.clamp(Mth.lerp(tickDelta, prevSize, currentSize), 1, MAX_SIZE);
    }

    public void setSize(float size) {
        entityData.set(TARGET_SIZE, size);
    }

    public float getTargetSize() {
        return entityData.get(TARGET_SIZE);
    }

    public int getSizeInBlocks() {
        return (int)(getBbWidth() * (getSize(1) / 15F));
    }

    @Override
    public void tick() {
        setRemainingFireTicks(1);

        prevSize = currentSize;
        float targetSize = getTargetSize();
        if (currentSize != targetSize) {
            float sizeDifference = (targetSize - currentSize);
            currentSize = Math.abs(sizeDifference) < 0.01F ? targetSize : currentSize + (sizeDifference * 0.02F);
        }

        if (isStormy()) {
            int stormTicks = getStormTicks();
            if (stormTicks > 0) {
                setStormTicks(stormTicks - 1);
            }
        }

        if (isLocalInstanceAuthoritative()) {
            float groundY = PosHelper.findNearestSurface(level(), blockPosition()).getY();
            float targetY = isStormy() ? STORMY_TARGET_ALTITUDE : CLEAR_TARGET_ALTITUDE;
            float cloudY = (float)getY() - targetY;

            push(0, 0.0003F * (groundY - cloudY), 0);

            if (!cursed && !isStormy()) {
                Vec3 wind = WeatherConditions.get(level()).getWindDirection();
                push(wind.x * 0.001F, 0, wind.z * 0.001F);
            }

            Vec3 velocity = getDeltaMovement();
            setPos(position().add(velocity.scale(0.7F)));
            setDeltaMovement(velocity.scale(0.9F));

            float randomYaw = random.nextFloat() * 360;
            Vec3 randomVelocity = Vec3.directionFromRotation(0, randomYaw).scale(0.002);

            for (var cloud : level().getEntitiesOfClass(StormCloudEntity.class, getBoundingBox(), EntitySelector.ENTITY_STILL_ALIVE)) {
                if (cloud != this) {
                    cloud.push(randomVelocity);
                }
            }
        }

        super.tick();

        float size = getSize(1);

        if (level().isClientSide()) {
            if (isStormy()) {
                WeatherConditions.get(level()).addStorm(this);

                float sizeInBlocks = getSizeInBlocks();
                int area = (int)Mth.square(sizeInBlocks);
                sizeInBlocks /= getBbWidth();
                for (int i = 0; i < area; i++) {
                    float x = (float)getRandomX(sizeInBlocks);
                    float z = (float)getRandomZ(sizeInBlocks);

                    level().addParticle(UParticles.RAIN_DROPS, x, getY(), z, 0, -0.2F, 0);
                }
            }
        } else {
            if (!isStormy() && !cursed && size > 10) {
                if (random.nextInt(1 + (int)(4000 / size)) == 0) {
                    split(random.nextIntBetweenInclusive(1, 3));
                    return;
                }
            }

            if (cursed) {
                float percent = Math.min(1, (size + 1) / MAX_SIZE);
                getBossBar().setProgress(percent);
            }

            if (currentSize == targetSize) {
                if (isDissipating()) {
                    if (size < 2) {
                        kill((ServerLevel)level());
                    } else {
                        if (random.nextInt(4) == 0) {
                            split(2);
                        } else {
                            setSize(size - 1);
                        }
                    }
                } else {
                    if (size < MAX_SIZE) {
                        if (cursed) {
                            setSize(size + 1);
                            setStormTicks(-1);

                            spawnLightningStrike(blockPosition(), false, false);
                            playSound(USounds.AMBIENT_WIND_GUST, 1, 1);
                        }
                    }
                }
            }

            if (size >= MAX_SIZE) {
                if (cursed) {
                    setStormTicks(-1);

                    if (nextPhase-- == 0) {
                        phase++;
                        nextPhase = random.nextIntBetweenInclusive(17, 120);

                        if (phase == 1) {
                            playSound(USounds.ENTITY_SOMBRA_SNICKER, 10, 1);
                        }

                        if (++phase >= 7) {
                            pickRandomPoints(13, pos -> spawnLightningStrike(pos, true, false));

                            cursed = false;
                            SombraEntity sombra = new SombraEntity(UEntities.SOMBRA, level(), getBossBar());
                            sombra.setPos(position());
                            sombra.setHomePos(level().getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, blockPosition()));
                            sombra.setScaleFactor(8);
                            sombra.stormCloud.set(this);
                            level().addFreshEntity(sombra);

                            playSound(USounds.ENTITY_SOMBRA_LAUGH, 10, 1);
                        } else {
                            pickRandomPoints(3, pos -> spawnLightningStrike(pos, true, false));
                        }
                    }
                } else {
                    if (!isStormy() && !tickClear()) {
                        setStormTicks(random.nextIntBetweenInclusive(30, 120));
                    }
                }
            }

            if (isStormy() && tickCount % 170 == 0) {
                pickRandomPoints(3, pos -> spawnLightningStrike(pos, false, random.nextInt(30) == 0));
            }
        }
    }

    private void pickRandomPoints(int count, Consumer<BlockPos> action) {
        BlockPos.randomInCube(random, 3, blockPosition(), getSizeInBlocks()).forEach(pos -> {
            action.accept(PosHelper.findNearestSurface(level(), pos));
        });
    }

    private void spawnLightningStrike(BlockPos pos, boolean cosmetic, boolean infect) {
        if (infect) {
            if (!CrystalShardsEntity.infestBlock((ServerLevel)level(), pos)) {
                return;
            }
        }
        LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(level(), EntitySpawnReason.EVENT);
        lightning.snapTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        lightning.setVisualOnly(cosmetic);
        level().addFreshEntity(lightning);

    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (!cursed) {
            if (random.nextInt(35) == 0 || (source.is(DamageTypes.PLAYER_ATTACK) && EquineContext.of(source.getEntity()).collidesWithClouds())) {
                if (getSize(1) < 2) {
                    if (!world.getGameRules().get(GameRules.MOB_DROPS)) {
                        getType().getDefaultLootTable().ifPresent(table -> {
                            LootParams.Builder builder = new LootParams.Builder(world)
                                    .withParameter(LootContextParams.THIS_ENTITY, this)
                                    .withParameter(LootContextParams.ORIGIN, this.position())
                                    .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                                    .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
                                    .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());
                            if (source.getEntity() instanceof Player player) {
                                builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).withLuck(player.getLuck());
                            }
                            registryAccess().lookupOrThrow(Registries.LOOT_TABLE).getValue(table)
                                .getRandomItems(builder.create(LootContextParamSets.ENTITY), 0L, stack -> spawnAtLocation(world, stack));
                        });
                    }
                    kill(world);
                    level().broadcastEntityEvent(this, EntityEvent.POOF);
                } else {
                    split(2 + random.nextInt(4));
                }
            }
        }
        return false;
    }

    @Override
    @Nullable
    public ItemEntity spawnAtLocation(ServerLevel world, ItemStack stack) {
        stack = stack.copy();
        while (!stack.isEmpty()) {
            ItemEntity drop = super.spawnAtLocation(world, stack.split(1));
            if (drop != null) {
                drop.push(random.triangle(0, 0.3), 0, random.triangle(0, 0.3));
            }
        }
        return null;
    }

    public void split(int splitCount) {
        Vec3 center = position();

        float totalAngle = (360F / splitCount) + ((float)random.nextGaussian() * 360F);
        float size = getSize(1) / splitCount;
        int stormTicks = getStormTicks() / splitCount;

        if (size < 1) {
            return;
        }
        discard();

        for (int i = 0; i < splitCount; i++) {
            StormCloudEntity lump = (StormCloudEntity)getType().create(level(), EntitySpawnReason.BREEDING);
            lump.setSize(size);
            lump.setStormTicks(stormTicks);
            lump.setPos(center);
            lump.setDissipating(isDissipating());
            lump.setDeltaMovement(Vec3.directionFromRotation(0, totalAngle * i).normalize().scale(1.8F));

            level().addFreshEntity(lump);
        }

    }

    @Override
    public void handleEntityEvent(byte status) {
        switch (status) {
            case EntityEvent.POOF:
                for (int i = 0; i < 20; ++i) {
                    double d = random.nextGaussian() * 0.02;
                    double e = random.nextGaussian() * 0.02;
                    double f = random.nextGaussian() * 0.02;
                    level().addParticle(ParticleTypes.POOF, getRandomX(1), getRandomY(), getRandomZ(1), d, e, f);
                }
                break;
            default:
                super.handleEntityEvent(status);
        }

    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public final boolean isPickable() {
        return true;
    }

    private ServerBossEvent getBossBar() {
        if (this.bossBar == null) {
            this.bossBar = SombraEntity.createBossBar(Component.translatable("unicopia.entity.sombra").withStyle(ChatFormatting.OBFUSCATED));
            trackingPlayers.removeIf(Entity::isRemoved);
            trackingPlayers.forEach(this.bossBar::addPlayer);
        }
        return this.bossBar;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput nbt) {
        nbt.putInt("stormTicks", getStormTicks());
        nbt.putInt("clearTicks", getClearTicks());
        nbt.putBoolean("dissipating", isDissipating());
        nbt.putFloat("size", getTargetSize());
        nbt.putBoolean("cursed", cursed);
        nbt.putInt("phase", phase);
        nbt.putInt("nextPhase", nextPhase);
    }

    @Override
    public void readAdditionalSaveData(ValueInput nbt) {
        setStormTicks(nbt.getIntOr("stormTicks", 0));
        setClearTicks(nbt.getIntOr("clearTicks", 0));
        setDissipating(nbt.getBooleanOr("dissipating", false));
        setSize(nbt.getFloatOr("size", getTargetSize()));
        cursed = nbt.getBooleanOr("cursed", false);
        phase = nbt.getIntOr("phase", 0);
        nextPhase = nbt.getIntOr("nextPhase", 0);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        trackingPlayers.removeIf(Entity::isRemoved);
        trackingPlayers.add(player);
        if (bossBar != null) {
            bossBar.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        trackingPlayers.removeIf(Entity::isRemoved);
        trackingPlayers.remove(player);
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }
    }

}
