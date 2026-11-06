package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.player.LevitatedItemsInventory;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.authlib.GameProfile;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.util.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LevitatingItemEntity extends TrapEntity implements Owned<Player>, MagicImmune {
    private static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(LevitatingItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> SLOT = SynchedEntityData.defineId(LevitatingItemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> OWNER_ID = SynchedEntityData.defineId(LevitatingItemEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    private static final EntityDataAccessor<Optional<BlockPos>> MINING_POS = SynchedEntityData.defineId(LevitatingItemEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Direction> MINING_FACE = SynchedEntityData.defineId(LevitatingItemEntity.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Integer> HOLDING_POSITION = SynchedEntityData.defineId(LevitatingItemEntity.class, EntityDataSerializers.INT);

    private Vec3 polarPositionOffset = Vec3.ZERO;
    private Vec3 manualPositionOffset = Vec3.ZERO;
    @Nullable
    private BlockState miningBlockState;
    @Nullable
    private BlockPos interactingPos;
    @Nullable
    private Vec3 holdPosition;

    @Nullable
    private LevitatedItemsInventory.BlockBreakingRecord blockBreakingRecord;

    private int swingTicks;

    @Nullable
    private Player master;

    private boolean ownderBodyYawSet;
    private float ownerBodyYaw;

    @Nullable
    private List<Action> validActions;

    LevitatingItemEntity(EntityType<? extends LevitatingItemEntity> type, Level world) {
        super(type, world);
    }

    public LevitatingItemEntity(int slot, Player player, ItemStack stack) {
        this(UEntities.LEVITATING_ITEM, player.level());
        setMaster(player);
        setStack(stack);
        setPos(player.position());
        setPolarPositionOffset(new Vec3(-0.5F, 0, 1.5).add(VecHelper.gaussian(level().getRandom()).scale(0.1)));
        setSlot(slot);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        builder.define(STACK, ItemStack.EMPTY);
        builder.define(OWNER_ID, Optional.empty());
        builder.define(SLOT, 0);
        builder.define(MINING_POS, Optional.empty());
        builder.define(MINING_FACE, Direction.UP);
        builder.define(HOLDING_POSITION, 0);
    }

    public void setSlot(int slot) {
        entityData.set(SLOT, slot);
    }

    public int getSlot() {
        return entityData.get(SLOT);
    }

    public void setPolarPositionOffset(Vec3 newOffset) {
        polarPositionOffset = newOffset;
    }

    public void setManualPositionOffset(Vec3 newOffset) {
        manualPositionOffset = newOffset;
    }

    public void setHoldingPosition(@Nullable Vec3 pos) {
        holdPosition = pos;
        entityData.set(HOLDING_POSITION, holdPosition == null ? 0 : 1);
    }

    public void setForcedHoldingPosition(boolean holding) {
        setHoldingPosition(holding ? position() : null);
        entityData.set(HOLDING_POSITION, holding ? 3 : 2);
    }

    public boolean isHoldingPosition() {
        return entityData.get(HOLDING_POSITION) % 2 == 1;
    }

    public boolean canChangePositionHoldingFreely() {
        return entityData.get(HOLDING_POSITION) > 1;
    }

    public Vec3 getRelativePosition() {
        return polarPositionOffset;
    }

    public Optional<BlockPos> getMiningPos() {
        return entityData.get(MINING_POS);
    }

    public Direction getMiningFace() {
        return entityData.get(MINING_FACE);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        var myDimensions = super.getDimensions(pose);
        if (this.isVehicle()) {
            var dimensions = getPassengers().getFirst().getDimensions(pose);
            return new EntityDimensions(
                    Math.max(dimensions.width() + 0.1F, myDimensions.width()),
                    Math.max(dimensions.height() * 0.7F, myDimensions.height()),
                    myDimensions.eyeHeight(), myDimensions.attachments(), false);
        }
        return myDimensions;
    }

    public Action getDefaultAction() {
        return Action.GRAB;
    }

    public List<Action> getValidActions() {
        if (validActions == null) {
            validActions = List.copyOf(Util.make(new ArrayList<>(), a -> recomputeValidActions(a::add)));
        }

        return validActions;
    }

    protected void recomputeValidActions(Consumer<Action> collector) {
        if (!isVehicle()) {
            collector.accept(Action.GRAB);
        }
        collector.accept(Action.MOVE);
        collector.accept(Action.DROP);
        collector.accept(isHoldingPosition() ? Action.FOLLOW : Action.TETHER);

        if (getMiningPos().isPresent()) {
            collector.accept(Action.STOP);
        }
    }

    public void handleAction(Action action, Player player, Optional<Vec3> direction) {
        switch (action) {
            case GRAB:
                if (!level().isClientSide()) {
                    player.addItem(getStack());
                }
                remove(RemovalReason.DISCARDED);
                playSound(SoundEvents.ITEM_PICKUP, 2, 1);
                break;
            case DROP:
                kill((ServerLevel)level());
                break;
            case FOLLOW:
                setHoldingPosition(null);
                break;
            case MOVE:
                if (direction.isPresent()) {
                    Vec3 movement = direction.get().normalize().scale(0.1F);
                    if (holdPosition != null) {
                        holdPosition = holdPosition.add(movement);
                    } else {
                        setManualPositionOffset(manualPositionOffset.add(movement));
                    }
                    move(MoverType.SELF, movement);
                }
                break;
            case MOVE_HORIZONTALLY:
                break;
            case MOVE_VERTICALLY:
                break;
            case STOP:
                getMiningPos().ifPresent(this::stopMining);
                break;
            case TETHER:
                setHoldingPosition(position());
                setForcedHoldingPosition(true);
                break;
        }
    }

    public void setMaster(Player player) {
        entityData.set(OWNER_ID, Optional.of(EntityReference.<LivingEntity>of(player.getUUID())));
        master = player;
    }

    @Override
    public @Nullable Player getMaster() {
        Optional<UUID> masterId = getMasterId();

        if (masterId.isEmpty()) {
            master = null;
            return null;
        }

        if (master == null || master.getUUID() != masterId.get()) {
            if (level() instanceof ServerLevel sw) {
                master = masterId.map(sw.getServer().getPlayerList()::getPlayer).orElse(null);
            } else {
                master = masterId.map(level()::getPlayerByUUID).orElse(null);
            }
        }

        return master;
    }

    @Override
    public Optional<UUID> getMasterId() {
        return entityData.get(OWNER_ID).map(EntityReference::getUUID);
    }

    public ItemStack getStack() {
        return this.entityData.get(STACK);
    }

    public void setStack(ItemStack stack) {
        this.entityData.set(STACK, stack);
    }

    public void swingAt(BlockPos pos) {
        swingTicks = 10;
        this.interactingPos = pos;
    }

    public double getAttackDamage(Player player) {
        ItemAttributeModifiers.Builder damageModifiersComponent = ItemAttributeModifiers.builder();
        getStack().forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute == Attributes.ATTACK_DAMAGE) {
                damageModifiersComponent.add(attribute, modifier, EquipmentSlotGroup.MAINHAND);
            }
        });

        ItemAttributeModifiers damageModifiers = damageModifiersComponent.build();

        if (damageModifiers.modifiers().isEmpty()) {
            return 0;
        }
        return damageModifiers.compute(Attributes.ATTACK_DAMAGE, player.getAttributeValue(Attributes.ATTACK_DAMAGE), EquipmentSlot.MAINHAND);
    }

    public boolean startMining(Player miner, LevitatedItemsInventory.BlockBreakingRecord blockBreakingRecord, Direction direction) {
        if (getMiningPos().isPresent()) {
            return false;
        }
        if (!getStack().getItem().canDestroyBlock(getStack(), blockBreakingRecord.state, level(), blockBreakingRecord.pos, miner)) {
            return false;
        }
        if (!blockBreakingRecord.state.requiresCorrectToolForDrops() || getStack().isCorrectToolForDrops(blockBreakingRecord.state)) {
            stopMining(blockBreakingRecord.pos);
            entityData.set(MINING_POS, Optional.of(blockBreakingRecord.pos));
            entityData.set(MINING_FACE, direction);
            this.blockBreakingRecord = blockBreakingRecord;
            blockBreakingRecord.claimants.add(this);
            setHoldingPosition(position());
            return true;
        }
        return false;
    }

    public void stopMining(BlockPos pos) {
        if (pos.equals(getMiningPos().orElse(null))) {
            entityData.set(MINING_POS, Optional.empty());
            miningBlockState = null;
            if (blockBreakingRecord != null) {
                blockBreakingRecord.claimants.remove(this);
            }
            blockBreakingRecord = null;
            setHoldingPosition(null);
            if (level() instanceof ServerLevel sw) {
                sw.destroyBlockProgress(getId(), pos, -1);
            }
        }
    }

    @Override
    public void tick() {
        @Nullable
        Player master = getMaster();
        if (master == null) {
            setInvisible(true);
            return;
        } else {
            setInvisible(false);

            if (master.isDeadOrDying() && level() instanceof ServerLevel sw) {
                kill(sw);
            }
        }

        refreshDimensions();

        if (getStack().isEmpty() && getPassengers().isEmpty()) {
            discard();
        }

        if (master.level() instanceof ServerLevel sw) {
            if (master.level() != level()) {
                Vec3 newPos = master.position().add(polarPositionOffset);
                teleportTo(sw, newPos.x, newPos.y, newPos.z, Relative.DELTA, 0, 0, false);
            }
        }

        super.tick();
        updatePosition(master);

        if (level() instanceof ServerLevel sw && getMiningPos().isPresent() && blockBreakingRecord != null) {
            tickMining(master, sw, blockBreakingRecord);
        }

        if (level().isClientSide()) {
            if (getRandom().nextInt(10) == 0) {
                ParticleUtils.spawnParticles(new MagicParticleEffect(CommonColors.WHITE), this, 1 + getRandom().nextInt(2));
            }
        }
    }

    private void tickMining(Player master, ServerLevel sw, LevitatedItemsInventory.BlockBreakingRecord blockBreakingRecord) {
        BlockState state = level().getBlockState(blockBreakingRecord.pos);
        lookAt(Anchor.EYES, Vec3.atCenterOf(blockBreakingRecord.pos));

        if (state.isAir() || !state.equals(blockBreakingRecord.state) || !master.mayInteract(sw, blockBreakingRecord.pos) || distanceToSqr(Vec3.atCenterOf(blockBreakingRecord.pos)) > 100) {
            stopMining(blockBreakingRecord.pos);
        } else {
            FakePlayer fakePlayer = FakePlayer.get(sw, new GameProfile(getMasterId().orElse(null), "[Levitated Item Entity " + getUUID().toString() + "]"));
            fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, getStack());
            blockBreakingRecord.breakingProgress += state.getDestroyProgress(fakePlayer, sw, blockBreakingRecord.pos);


            if (tickCount % 4 == 0) {
                SoundType group = state.getSoundType();
                sw.playSound(null, blockBreakingRecord.pos, state.getSoundType().getHitSound(), SoundSource.BLOCKS,
                        (group.getVolume() + 1) / 8F,
                        group.getPitch() / 2F);
            }

            if (blockBreakingRecord.breakingProgress >= 1) {
                if (!fakePlayer.gameMode.destroyBlock(blockBreakingRecord.pos)) {
                    ((ServerPlayer)master).connection.send(new ClientboundBlockUpdatePacket(blockBreakingRecord.pos, sw.getBlockState(blockBreakingRecord.pos)));
                }
                stopMining(blockBreakingRecord.pos);
            } else {
                int stage = (int)(blockBreakingRecord.breakingProgress * 10F);
                if (stage != blockBreakingRecord.breakingStage) {
                    blockBreakingRecord.breakingStage = stage;
                    sw.destroyBlockProgress(getId(), blockBreakingRecord.pos, blockBreakingRecord.breakingStage);
                }
            }
        }
    }

    private void updatePosition(Player master) {
        if (isLocalInstanceAuthoritative()) {

            if (!master.isSecondaryUseActive() && holdPosition != null && blockBreakingRecord == null && distanceToSqr(master) < 4 && canChangePositionHoldingFreely()) {
                setHoldingPosition(null);
            }

            boolean isBeingLookedAt = Pony.of(master).isLookingAt(this) || isPassengerOfSameVehicle(master);

            if (!ownderBodyYawSet || master.getDeltaMovement().horizontalDistance() > 0.5F) {
                ownerBodyYaw = master.getVisualRotationYInDegrees();
                ownderBodyYawSet = true;
            }

            Vec3 targetPosition = isBeingLookedAt ? position().add(0, Mth.sin(tickCount / 15F) * 0.02F, 0) : (holdPosition == null ? master.getEyePosition().add(calculateViewVector(
                    (float)polarPositionOffset.x * Mth.RAD_TO_DEG,
                    ownerBodyYaw + (float)polarPositionOffset.z * Mth.RAD_TO_DEG
            )).add(manualPositionOffset) : holdPosition).add(0, Mth.sin(tickCount / 15F) * 0.2F + 0.2F, 0);

            BlockPos miningPos = getMiningPos().orElse(null);

            if (miningPos != null) {
                targetPosition = VecHelper.lerp(getSwingProgress(), targetPosition, Vec3.atCenterOf(miningPos));
            }
            if (interactingPos != null) {
                targetPosition = VecHelper.lerp(getSwingProgress(), Vec3.atCenterOf(interactingPos), targetPosition);
                if (swingTicks <= -10) {
                    interactingPos = null;
                }
            }

            move(MoverType.SELF, targetPosition.subtract(position()).scale(0.3));
            syncPacketPositionCodec(getX(), getY(), getZ());
        }

        getInterpolation().interpolate();
    }

    @Override
    public void move(MoverType movementType, Vec3 movement) {
        if (!noPhysics) {
            if (isVehicle()) {
                Entity passenger = getPassengers().get(0);
                AABB box = passenger.getBoundingBox();
                Vec3 adjustedMovement = Entity.collideBoundingBox(passenger, movement, box, level(), level().getEntityCollisions(passenger, box.expandTowards(movement)));
                if (!adjustedMovement.equals(movement)) {
                    Vec3 downMove = movement.add(0, master.getY() - getY() - 1, 0);
                    adjustedMovement = Entity.collideBoundingBox(passenger, downMove, box, level(), level().getEntityCollisions(passenger, box.expandTowards(downMove)));
                }

                movement = adjustedMovement;
            }
        }
        super.move(movementType, movement);
    }

    private float getSwingProgress() {
        if (getMiningPos().isPresent()) {
            return (tickCount % 10F) / 10F;
        }

        if (isSwinging()) {
            swingTicks--;
            return Mth.clamp(Math.abs(swingTicks) / 10F, 0, 1);
        }

        return 0;
    }

    public boolean isSwinging() {
        return swingTicks > -10;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitPos) {
        InteractionManager.getInstance().interactLevitatingItem(this, player);
        return InteractionResult.PASS;
    }

    public boolean attemptDismount(Entity passenger) {
        if (!(passenger instanceof LivingEntity living)) {
            return true;
        }
        Pony master = Pony.of(getMaster());
        if (master == null) {
            return true;
        }
        playSound(USounds.ENTITY_PLAYER_KICK, 0.6F, (float)random.triangle(0.6, 0.3));
        level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, blockPosition(), Block.getId(Blocks.SLIME_BLOCK.defaultBlockState()));
        gameEvent(GameEvent.ENTITY_ACTION, passenger);
        if (level() instanceof ServerLevel sw) {
            master.asEntity().hurtServer(sw, passenger instanceof Player player ? damageSources().playerAttack(player) : damageSources().mobAttack(living), 0.5F);
        }
        return master.subtractEnergyCost(10);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (HOLDING_POSITION.equals(data) || MINING_POS.equals(data)) {
            validActions = null;
        }

        if (level().isClientSide()) {
            if (OWNER_ID.equals(data) || SLOT.equals(data)) {
                if (master != null) {
                    Pony.of(master).getLevitatingItems().onEntityDespawned(this);
                }
                Player master = getMaster();
                if (master != null) {
                    Pony.of(master).getLevitatingItems().onEntitySpawned(this);
                }
            }
        }
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason == Entity.RemovalReason.KILLED) {
            dropAsItemEntity();
        }
        super.remove(reason);
    }

    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    public void dropAsItemEntity() {
        if (level() instanceof ServerLevel sw) {
            Vec3 dropPos = position();
            sw.addFreshEntity(new ItemEntity(sw, dropPos.x, dropPos.y, dropPos.z, getStack()));
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput nbt) {
        nbt.read("stack", ItemStack.CODEC).ifPresent(this::setStack);
        entityData.set(OWNER_ID, nbt.read("owner", UUIDUtil.AUTHLIB_CODEC).map(EntityReference::<LivingEntity>of));
        setPolarPositionOffset(nbt.read("polarPositionOffset", CodecUtils.POSITION_VECTOR).orElse(Vec3.ZERO));
        setManualPositionOffset(nbt.read("manualPositionOffset", CodecUtils.POSITION_VECTOR).orElse(Vec3.ZERO));
        setHoldingPosition(nbt.read("holdPosition", CodecUtils.POSITION_VECTOR).orElse(Vec3.ZERO));

        setForcedHoldingPosition(holdPosition != null && nbt.getBooleanOr("movementRestricted", false));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput nbt) {
        if (!getStack().isEmpty()) {
            nbt.store("stack", ItemStack.CODEC, getStack());
        }
        nbt.storeNullable("owner", UUIDUtil.AUTHLIB_CODEC, getMasterId().orElse(null));
        nbt.store("polarPositionOffset", CodecUtils.POSITION_VECTOR, polarPositionOffset);
        nbt.store("manualPositionOffset", CodecUtils.POSITION_VECTOR, manualPositionOffset);
        nbt.storeNullable("holdPosition", CodecUtils.POSITION_VECTOR, holdPosition);
        nbt.putBoolean("movementRestricted", !canChangePositionHoldingFreely());
    }

    @Override
    public void restoreFrom(Entity original) {
        super.restoreFrom(original);
        if (original instanceof LevitatingItemEntity l) {
            setSlot(l.getSlot());
        }
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean shouldBeSaved() {
        return isVehicle() && super.shouldBeSaved();
    }

    @Override
    public boolean fireImmune() {
        return !getStack().canBeHurtBy(damageSources().inFire()) || super.fireImmune();
    }

    @Override
    public final boolean hurtClient(DamageSource source) {
        return !isInvulnerable() && !isInvulnerableToBase(source) && getStack().canBeHurtBy(source);
    }

    @Override
    public final boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (isInvulnerable() || isInvulnerableToBase(source)) {
            return false;
        } else if (!getStack().canBeHurtBy(source)) {
            return false;
        }
        kill(world);
        return true;
    }

    @Override
    public boolean isInvulnerable() {
        return tickCount < 50 || super.isInvulnerable();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entry) {
        Player master = getMaster();
        return new ClientboundAddEntityPacket(this, entry, master == null ? 0 : master.getId());
    }

    @Override
    public void onClientRemoval() {
        @Nullable
        Player master = getMaster();
        if (master != null) {
            Pony.of(master).getLevitatingItems().onEntityDespawned(this);
        }
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        if (level().getEntity(packet.getData()) instanceof Player m) {
            master = m;
        }
    }

    public enum Action {
        MOVE,
        MOVE_VERTICALLY,
        MOVE_HORIZONTALLY,
        DROP,
        GRAB,
        FOLLOW,
        TETHER,
        STOP;

        private final Component label = Component.translatable("entity.unicopia.levitating_item.action." + name().toLowerCase(Locale.ROOT));

        public int getU() {
            return (ordinal() % 4);
        }

        public int getV() {
            return ordinal() / 4;
        }

        public Component getLabel() {
            return label;
        }
    }
}
