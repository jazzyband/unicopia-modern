package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.mixin.MixinBlockEntity;
import com.minelittlepony.unicopia.util.InventoryUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.Containers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class MimicEntity extends PathfinderMob {
    static final byte OPEN_MOUTH = (byte)200;
    static final byte CLOSE_MOUTH = (byte)201;
    static final EntityDataSerializer<CompoundTag> CHEST_DATA_SERIALIZER = EntityDataSerializer.forValueType(ByteBufCodecs.COMPOUND_TAG);
    static final EntityDataAccessor<CompoundTag> CHEST_DATA = SynchedEntityData.defineId(MimicEntity.class, CHEST_DATA_SERIALIZER);
    static final EntityDataAccessor<Boolean> MOUTH_OPEN = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.BOOLEAN);

    static {
        FabricEntityDataRegistry.register(Unicopia.id("mimic_chest_data"), CHEST_DATA_SERIALIZER);
    }

    @Nullable
    private ChestBlockEntity chestData;

    private int openTicks;
    private final Set<Player> observingPlayers = new HashSet<>();

    static void bootstrap() {
        PlayerBlockBreakEvents.BEFORE.register((Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) -> {
            if (blockEntity instanceof MimicEntity.MimicGeneratable generatable) {
                var mimic = generatable.triggerMimic(player);
                if (mimic != null) {
                    player.attack(mimic);
                    return false;
                }
            }

            return true;
        });
    }

    public static boolean shouldConvert(Level world, BlockPos pos, Player player, ResourceKey<LootTable> lootTable) {
        if (!shouldGenerateMimic(lootTable)
                || !world.getBlockState(pos).is(UTags.Blocks.MIMIC_CHESTS)
                || !(world.getBlockEntity(pos) instanceof ChestBlockEntity be)
                || be.getBlockState().getOptionalValue(ChestBlock.TYPE).orElse(ChestType.SINGLE) != ChestType.SINGLE) {
            return false;
        }

        if (!(world instanceof ServerLevel serverWorld)) {
            return false;
        }
        var localDifficulty = serverWorld.getCurrentDifficultyAt(pos);
        if (localDifficulty.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        float difficulty = Mth.clamp(localDifficulty.getEffectiveDifficulty(), 0F, 4F) / 4F;
        float threshold = 0.8F * ((EnchantmentUtil.getLuck(0, player) / 20F) + 0.5F);
        return threshold * world.getRandom().nextFloat() < difficulty;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public static MimicEntity spawnFromChest(Level world, BlockPos pos) {
        if (!(world.getBlockEntity(pos) instanceof ChestBlockEntity be)) {
            return null;
        }
        world.removeBlockEntity(pos);
        world.setBlockEntity(be.getType().getBlockEntity(world, pos));
        world.removeBlock(pos, true);
        MimicEntity mimic = UEntities.MIMIC.create(world, EntitySpawnReason.NATURAL);
        BlockState state = be.getBlockState();
        Direction facing = state.getOptionalValue(ChestBlock.FACING).orElse(null);
        float yaw = facing.toYRot();
        be.setBlockState(be.getBlockState().getBlock().defaultBlockState());
        mimic.absSnapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        mimic.setYHeadRot(yaw);
        mimic.setYBodyRot(yaw);
        mimic.setYRot(yaw);
        mimic.setChest(be);
        world.addFreshEntity(mimic);
        return mimic;
    }

    public static boolean shouldGenerateMimic(@Nullable ResourceKey<LootTable> lootTable) {
        return lootTable != null
             && lootTable.identifier().getPath().indexOf("village") == -1
             && lootTable.identifier().getPath().indexOf("bastion") == -1
             && lootTable.identifier().getPath().indexOf("underwater") == -1
             && lootTable.identifier().getPath().indexOf("shipwreck") == -1
             && !BuiltInLootTables.SPAWN_BONUS_CHEST.equals(lootTable);
    }

    MimicEntity(EntityType<? extends MimicEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHEST_DATA, new CompoundTag());
        builder.define(MOUTH_OPEN, false);
    }

    @Override
    public boolean canBeCollidedWith(net.minecraft.world.entity.Entity other) {
        return isAlive();
    }

    @Override
    public boolean isPickable() {
        return isAlive();
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        Item item = chestData.getBlockState().getBlock().asItem();
        return item == Items.AIR ? null : item.getDefaultInstance();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(2, new AttackGoal(this, 0.6F, false));
    }

    public void setChest(ChestBlock chest) {
        if (chest.newBlockEntity(blockPosition(), chest.defaultBlockState()) instanceof ChestBlockEntity be) {
            setChest(be);
            be.setLootTable(BuiltInLootTables.ABANDONED_MINESHAFT, level().getRandom().nextLong());
            if (!level().isClientSide()) {
                entityData.set(CHEST_DATA, writeChestData(chestData));
            }
        }
    }

    public void setChest(@Nullable ChestBlockEntity chestData) {
        this.chestData = chestData;
        if (chestData != null) {
            ((MimicGeneratable)chestData).setAllowMimics(false);
            chestData.setLevel(level());
        }
        if (!level().isClientSide()) {
            entityData.set(CHEST_DATA, chestData == null ? new CompoundTag() : writeChestData(chestData));
        }
    }

    @Nullable
    public ChestBlockEntity getChestData() {
        return chestData;
    }

    public boolean isMouthOpen() {
        return entityData.get(MOUTH_OPEN);
    }

    public void setMouthOpen(boolean mouthOpen) {
        if (mouthOpen == isMouthOpen()) {
            return;
        }

        playSound(mouthOpen ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, 0.5F, 1F);
        entityData.set(MOUTH_OPEN, mouthOpen);
        if (chestData != null) {
            chestData.triggerEvent(1, mouthOpen ? 1 : 0);
        }
    }

    public void playChompAnimation() {
        openTicks = 5;
        setMouthOpen(true);
    }

    public float getPeekAmount() {
        return Mth.clamp((float)getDeltaMovement().lengthSqr() * 50F, 0, 1);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            if (tickCount < 12 || (getTarget() == null && this.attackStrengthTicker < tickCount - 30)) {
                BlockPos pos = blockPosition();
                setPos(
                        pos.getX() + 0.5,
                        getY(),
                        pos.getZ() + 0.5
                );
                setYBodyRot(Mth.floor(getVisualRotationYInDegrees() / 90) * 90);
                setYRot(Mth.floor(getYRot() / 90) * 90);
                setYHeadRot(Mth.floor(getYHeadRot() / 90) * 90);
                if (getHealth() < getMaxHealth() && level().getRandom().nextInt(20) == 0) {
                    heal(1);
                } else if (tickCount % 150 == 0 && chestData != null && !isMouthOpen()) {
                    if (level().getNearestPlayer(this, 15) == null) {
                        level().setBlockAndUpdate(blockPosition(), chestData.getBlockState().trySetValue(ChestBlock.FACING, getDirection()));
                        if (level().getBlockEntity(blockPosition()) instanceof ChestBlockEntity be) {
                            InventoryUtil.copyInto(chestData, be);
                            ((MimicGeneratable)be).setMimic(true);
                            discard();
                        }
                    }
                }
            }

            if (!observingPlayers.isEmpty()) {
                setMouthOpen(true);
            }
        }

        if (chestData == null) {
            setChest((ChestBlock)Blocks.CHEST);
        }

        if (level().isClientSide()) {
            ((MixinBlockEntity)chestData).setPos(blockPosition());
            ChestBlockEntity.lidAnimateTick(level(), blockPosition(), chestData.getBlockState(), chestData);
        }

        if (openTicks > 0 && --openTicks <= 0) {
            setMouthOpen(false);
        }

        if (getTarget() != null) {
            if (openTicks <= 0 && tickCount % 20 == 0) {
                playChompAnimation();
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (getTarget() == null && chestData != null) {
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return chestData.getDisplayName();
                }

                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                    return createScreenHandler(syncId, inv, player);
                }
            });

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public AbstractContainerMenu createScreenHandler(int syncId, Inventory inv, Player player) {
        return ChestMenu.threeRows(syncId, inv, new MimicInventory(chestData, player));
    }

    @Override
    public void setAggressive(boolean attacking) {
        super.setAggressive(attacking);
        if (attacking) {
            playChompAnimation();
        }
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        playChompAnimation();
        if (source.getEntity()  instanceof LivingEntity l) {
            setTarget(l);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (CHEST_DATA.equals(data) && level().isClientSide()) {
            setChest(readChestData(entityData.get(CHEST_DATA)));
        } else if (MOUTH_OPEN.equals(data)) {
            if (chestData != null) {
                chestData.triggerEvent(1, isMouthOpen() ? 1 : 0);
            }
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        setChest(nbt.read("chest", CompoundTag.CODEC).map(this::readChestData).orElse(null));
    }

    @Nullable
    private ChestBlockEntity readChestData(CompoundTag nbt) {
        @Nullable BlockState state = nbt.read("state", BlockState.CODEC).orElse(null);
        if (state != null && BlockEntity.loadStatic(blockPosition(), state, nbt.getCompoundOrEmpty("data"), registryAccess()) instanceof ChestBlockEntity data) {
            data.setLevel(level());
            ((MimicGeneratable)data).setAllowMimics(false);
            return data;
        }
        return null;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        if (chestData != null) {
            nbt.store("chest", CompoundTag.CODEC, writeChestData(chestData));
        }
    }

    @Nullable
    private CompoundTag writeChestData(ChestBlockEntity chestData) {
        CompoundTag chest = new CompoundTag();
        var data = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, registryAccess());
        chestData.saveWithId(data);
        chest.put("data", data.buildResult());
        chest.put("state", BlockState.CODEC.encode(chestData.getBlockState(), NbtOps.INSTANCE, new CompoundTag()).result().get());
        return chest;
    }

    @Override
    protected void dropFromLootTable(ServerLevel world, DamageSource damageSource, boolean causedByPlayer) {
        if (chestData != null) {
            Containers.dropContents(level(), this, chestData);
            Containers.dropItemStack(level(), getX(), getY(), getZ(), chestData.getBlockState().getBlock().asItem().getDefaultInstance());
        }
    }

    public class AttackGoal extends MeleeAttackGoal {
        private int ticks;

        public AttackGoal(PathfinderMob mob, double speed, boolean pauseWhenMobIdle) {
            super(mob, speed, pauseWhenMobIdle);
        }

        @Override
        public void start() {
            super.start();
            this.ticks = 0;
        }

        @Override
        public void stop() {
            super.stop();
            setAggressive(false);
        }

        @Override
        public void tick() {
            super.tick();
            setAggressive(++ticks >= 5 && getTicksUntilNextAttack() < getAttackInterval() / 2);
        }
    }

    public interface MimicGeneratable {
        void setAllowMimics(boolean allowMimics);

        void setMimic(boolean mimic);

        void readMimicAttributes(net.minecraft.world.level.storage.ValueInput nbt);

        void writeMimicAttributes(net.minecraft.world.level.storage.ValueOutput nbt);

        void configureMimic(@Nullable Player player);

        @Nullable
        MimicEntity triggerMimic(@Nullable Player player);
    }

    private final class MimicInventory implements Container {
        private final ChestBlockEntity chestData;
        private final Player player;

        public MimicInventory(ChestBlockEntity chestData, Player player) {
            this.chestData = chestData;
            this.player = player;
        }

        @Override
        public void startOpen(ContainerUser user) {
            if (user instanceof Player player) observingPlayers.add(player);
            setMouthOpen(true);
        }

        @Override
        public void stopOpen(ContainerUser user) {
            if (user instanceof Player player) observingPlayers.remove(player);
            setMouthOpen(!observingPlayers.isEmpty());
        }

        @Override
        public void clearContent() {
            chestData.clearContent();
        }

        @Override
        public int getContainerSize() {
            return chestData.getContainerSize();
        }

        @Override
        public boolean isEmpty() {
            return chestData.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return chestData.getItem(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = chestData.removeItem(slot, amount);
            if (!stack.isEmpty()) {
                setChanged();
            }
            return stack;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = chestData.removeItemNoUpdate(slot);
            if (!stack.isEmpty()) {
                setChanged();
            }
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            ItemStack oldStack = getItem(slot);
            if (!ItemStack.matches(stack, oldStack)) {
                chestData.setItem(slot, ItemStack.EMPTY);
                if (chestData.getLevel() instanceof ServerLevel sw) {
                    spawnAtLocation(sw, stack);
                }
                setChanged();
            }
        }

        @Override
        public void setChanged() {
            observingPlayers.clear();
            playChompAnimation();
            setTarget(player);
            if (player instanceof ServerPlayer spe) {
                spe.closeContainer();
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return true;//chestData.canPlayerUse(player);
        }
    }
}
