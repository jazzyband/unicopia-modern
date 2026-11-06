package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.component.BufferflyVariantComponent;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ButterflyEntity extends AmbientCreature {
    private static boolean isHoldingFlower(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem blockItem
                && blockItem.getBlock().builtInRegistryHolder().is(BlockTags.FLOWERS);
    }

    private static final int MAX_BREEDING_COOLDOWN = 300;
    private static final int MAX_REST_TICKS = 40;
    private static final int BREEDING_INTERVAL = 20;
    private static final int FLOWER_DETECTION_RANGE = 10;
    private static final int FLOWER_UPDATE_INTERVAL = 100;

    private static final EntityDataAccessor<Boolean> RESTING = SynchedEntityData.defineId(ButterflyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(ButterflyEntity.class, EntityDataSerializers.INT);

    private Optional<BlockPos> hoveringPosition = Optional.empty();
    private Optional<BlockPos> flowerPosition = Optional.empty();

    private final Map<BlockPos, Long> visited = new HashMap<>();

    private int ticksResting;
    private int breedingCooldown;

    public ButterflyEntity(EntityType<ButterflyEntity> type, Level world) {
        super(type, world);
        setVariant(Variant.random(world.getRandom()));
        setResting(true);
    }

    public static AttributeSupplier.Builder createButterflyAttributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 2);
    }

    public static boolean canSpawn(EntityType<? extends ButterflyEntity> type, LevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        return world.getBlockState(pos.below()).is(UTags.Blocks.BUTTERFLIES_SPAWNABLE_ON);
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.95F;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return USounds.ENTITY_BUTTERFLY_HURT;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, Variant.BUTTERFLY.ordinal());
        builder.define(RESTING, false);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) { }

    @Override
    protected void pushEntities() { }

    @Override
    public void tick() {
        super.tick();

        Vec3 vel = getDeltaMovement();
        setDeltaMovement(vel.x, vel.y * 0.6 + 0.02F, vel.z);
    }

    public boolean isResting() {
        return getEntityData().get(RESTING);
    }

    public void setResting(boolean resting) {
        getEntityData().set(RESTING, resting);
        if (!resting) {
            hoveringPosition = Optional.empty();
            flowerPosition = Optional.empty();
        }
    }

    public Variant getVariant() {
        return Variant.byId(getEntityData().get(VARIANT));
    }

    public void setVariant(Variant variant) {
        getEntityData().set(VARIANT, variant.ordinal());
    }

    protected boolean isAggressor(Entity e) {
        if (e instanceof ButterflyEntity) {
            return false;
        }

        if (e instanceof Player) {
            Player player = (Player)e;

            if (isHoldingFlower(player)) {
                setTarget(player);
                return false;
            }

            if (player.isCreative() || player.isSpectator()) {
                return false;
            }

            if (player.isSprinting() || player.zza > 0 || player.xxa > 0) {
                return true;
            }
        } else if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)) {
            return false;
        }

        return e.getDeltaMovement().horizontalDistance() > 1.4F;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (breedingCooldown > 0) {
            breedingCooldown--;
        }

        BlockPos below = BlockPos.containing(position().add(0, -0.5, 0));

        visited.entrySet().removeIf(e -> e.getValue() < tickCount - 500);

        if (isResting()) {
            if (!flowerPosition.isPresent()) {
                setResting(false);
                return;

            }

            if (level().getBlockState(below).isAir()
                || !level().getEntities(this, getBoundingBox().inflate(3), this::isAggressor).isEmpty()
                || (ticksResting++ > MAX_REST_TICKS || level().getRandom().nextInt(500) == 0)
                || level().isRainingAt(below)) {
                setResting(false);
                return;
            }

            if (!level().isClientSide()
                    && tickCount % BREEDING_INTERVAL == 0
                    && level().getRandom().nextInt(200) == 0
                    && canBreed()) {
                breed();
            }
        } else {
            ticksResting = 0;

            if (getTarget() instanceof Player player) {
                if (player.isRemoved() || !isHoldingFlower(player)) {
                    setTarget(null);
                }
                if (distanceTo(player) > 3) {
                    moveTowards(player.blockPosition());
                } else {
                    this.push(random.nextFloat() * 0.1 - 0.05F, random.nextFloat() * 0.1, random.nextFloat() * 0.1 - 0.05F);
                }
            } else {

                updateFlowerPosition().map(flower -> {
                    if (flower.closerToCenterThan(position(), 1)) {
                        setResting(true);
                        visited.put(flower, (long)tickCount);
                        if (breedingCooldown <= 0) {
                            breedingCooldown = MAX_BREEDING_COOLDOWN / 10;
                        }
                    }

                    return flower;
                }).or(this::findNextHoverPosition).ifPresent(this::moveTowards);

                if (random.nextInt(100) == 0 && level().getBlockState(below).canOcclude()) {
                    setResting(true);
                }
            }
        }
    }

    private boolean canBreed() {
        return tickCount > BREEDING_INTERVAL && breedingCooldown <= 0 && isResting() && level().getEntities(this, getBoundingBox().inflate(2), i -> {
            return i instanceof ButterflyEntity && i.getType() == getType() && ((ButterflyEntity)i).isResting();
        }).size() == 1;
    }

    private boolean breed() {
        breedingCooldown = MAX_BREEDING_COOLDOWN;

        ButterflyEntity copy = (ButterflyEntity)getType().create(level(), EntitySpawnReason.BREEDING);
        copy.copyPosition(this);
        level().addFreshEntity(copy);
        setResting(false);
        return true;
    }

    private Optional<BlockPos> findNextHoverPosition() {
        // invalidate the hovering position
        BlockPos pos = blockPosition();

        return hoveringPosition = hoveringPosition.filter(p -> level().isEmptyBlock(p)
                && p.getY() >= 1
                && random.nextInt(30) != 0
                && p.distSqr(pos) >= 4).or(() -> {
            return Optional.of(pos.offset(
                    random.nextInt(7) - random.nextInt(7),
                    random.nextInt(6) - 2,
                    random.nextInt(7) - random.nextInt(7)
            ));
        });
    }

    private Optional<BlockPos> updateFlowerPosition() {

        if (tickCount > 0 && tickCount % FLOWER_UPDATE_INTERVAL != 0) {
            return flowerPosition;
        }

        flowerPosition = flowerPosition.filter(p -> level().getBlockState(p).is(BlockTags.FLOWERS)).or(() -> {
            return com.minelittlepony.unicopia.util.PosHelper.withinManhattanStream(blockPosition(), FLOWER_DETECTION_RANGE, FLOWER_DETECTION_RANGE, FLOWER_DETECTION_RANGE)
                    .filter(p -> !visited.containsKey(p) && level().getBlockState(p).is(BlockTags.FLOWERS))
                    .findFirst()
                    .map(p -> {
                visited.put(p, (long)tickCount - 900);
                return p;
            });
        });

        return flowerPosition;
    }

    private void moveTowards(BlockPos pos) {
        Vec3 motion = Vec3.upFromBottomCenterOf(pos, 0.1).subtract(position());
        Vec3 vel = getDeltaMovement();

        push(
            (Math.signum(motion.x()) * 0.5 - vel.x) * 0.1,
            (Math.signum(motion.y()) * 0.7 - vel.y) * 0.1,
            (Math.signum(motion.z()) * 0.5 - vel.z) * 0.1
        );

        float direction = (float)(Mth.atan2(vel.z, vel.x) * (180 / Math.PI)) - 90;

        zza = 0.5F;
        yHeadRot += Mth.wrapDegrees(direction - yHeadRot);
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
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor world, EntitySpawnReason reason) {
        return reason != EntitySpawnReason.NATURAL || (getY() >= world.getSeaLevel() && world.getMaxLocalRawBrightness(blockPosition()) > 3);
    }

    @Override
    public ItemEntity spawnAtLocation(ServerLevel world, ItemStack stack, float yOffset) {
        return super.spawnAtLocation(world, BufferflyVariantComponent.set(stack, new BufferflyVariantComponent(getVariant(), true)), yOffset);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("ticksResting", ticksResting);
        nbt.putInt("breedingCooldown", breedingCooldown);
        nbt.storeNullable("hoveringPosition", BlockPos.CODEC, hoveringPosition.orElse(null));
        nbt.storeNullable("flowerPosition", BlockPos.CODEC, flowerPosition.orElse(null));
        ValueOutput visited = nbt.child("visited");
        this.visited.forEach((pos, time) -> {
            visited.putLong(String.valueOf(pos.asLong()), time);
        });
    }

    @Override
    public void readAdditionalSaveData(ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        ticksResting = nbt.getIntOr("ticksResting", 0);
        breedingCooldown = nbt.getIntOr("breedingCooldown", 0);
        hoveringPosition = nbt.read("hoveringPosition", BlockPos.CODEC);
        flowerPosition = nbt.read("flowerPosition", BlockPos.CODEC);
        ValueInput visited = nbt.childOrEmpty("visited");
        this.visited.clear();
        visited.keySet().forEach(key -> {
            try {
                this.visited.put(BlockPos.of(Long.valueOf(key)), visited.getLongOr(key, 0));
            } catch (NumberFormatException ignore) {}
        });
    }

    public enum Variant implements StringRepresentable {
        BUTTERFLY,
        YELLOW,
        LIME,
        RED,
        GREEN,
        BLUE,
        PURPLE,
        MAGENTA,
        PINK,
        HEDYLIDAE,
        LYCAENIDAE,
        NYMPHALIDAE,
        MONARCH,
        WHITE_MONARCH,
        BRIMSTONE;

        public static final Variant[] VALUES = values();

        public static final Codec<Variant> CODEC = StringRepresentable.fromEnum(Variant::values);
        public static final StreamCodec<ByteBuf, Variant> PACKET_CODEC = ByteBufCodecs.idMapper(i -> VALUES[i], Variant::ordinal);

        private final String name = name().toLowerCase(Locale.ROOT);
        private final Identifier skin = Unicopia.id("textures/entity/butterfly/" + name + ".png");

        public Identifier getSkin() {
            return skin;
        }

        public static Variant byId(int index) {
            return VALUES[Math.max(0, index) % VALUES.length];
        }

        static Variant random(RandomSource rand) {
            return VALUES[rand.nextInt(VALUES.length)];
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
