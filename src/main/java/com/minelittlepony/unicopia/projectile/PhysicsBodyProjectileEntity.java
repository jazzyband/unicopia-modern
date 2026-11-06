package com.minelittlepony.unicopia.projectile;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.UItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class PhysicsBodyProjectileEntity extends AbstractArrow implements ItemSupplier {

    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(PhysicsBodyProjectileEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> BOUNCY = SynchedEntityData.defineId(PhysicsBodyProjectileEntity.class, EntityDataSerializers.BOOLEAN);

    private int inWaterTime;

    private ResourceKey<DamageType> damageType = UDamageTypes.ROCK;

    public PhysicsBodyProjectileEntity(EntityType<PhysicsBodyProjectileEntity> type, Level world, ItemStack stack) {
        super(type, 0, 0, 0, world, stack, null);
        setPickupItemStack(stack);
    }

    public PhysicsBodyProjectileEntity(Level world, ItemStack stack) {
        this(UEntities.MUFFIN, world, stack);
    }

    public PhysicsBodyProjectileEntity(Level world, @Nullable LivingEntity thrower, ItemStack stack) {
        super(UEntities.MUFFIN, thrower, world, stack, null);
        setPickupItemStack(stack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ITEM, ItemStack.EMPTY).define(BOUNCY, false);
    }

    @Override
    public void setPickupItemStack(ItemStack stack) {
        getEntityData().set(ITEM, stack.isEmpty() ? getDefaultPickupItem() : stack.copy());
        super.setPickupItemStack(stack);
    }

    @Override
    public ItemStack getItem() {
        return getEntityData().get(ITEM);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return UItems.MUFFIN.getDefaultInstance();
    }

    public void setDamageType(ResourceKey<DamageType> damageType) {
        this.damageType = damageType;
    }

    @Override
    protected ItemStack getPickupItem() {
        return getItem().copy();
    }

    @Override
    public ItemStack getPickupItemStackOrigin() {
        return getItem();
    }

    public void setBouncy() {
        getEntityData().set(BOUNCY, true);
    }

    public boolean isBouncy() {
        return getEntityData().get(BOUNCY);
    }

    @Override
    public void tick() {
        super.tick();
        if (inGroundTime > 0) {
            Vec3 vel = getDeltaMovement();
            vel = vel.multiply(0, 1, 0);

            move(MoverType.SELF, vel);

            setDeltaMovement(vel.scale(0.3));
            push(0, -0.025, 0);
        }

        if (isBouncy() && isInWater()) {
            setDeltaMovement(getDeltaMovement().scale(0.3).add(0, 0.05125, 0));
            inWaterTime++;
        } else {
            inWaterTime = 0;
        }
    }

    @Override
    public void playerTouch(Player player) {

        if (level().isClientSide() || isNoPhysics() || shakeTime > 0) {
            return;
        }

        if (inWaterTime <= 0) {
            super.playerTouch(player);
        } else if (tryPickup(player)) {
            player.take(this, 1);
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (isBouncy()) {
            setDeltaMovement(getDeltaMovement().multiply(-0.1, -0.3, -0.1));
            setYRot(getYRot() + 180);
            yRotO += 180;
            return;
        } else {
            ItemStack stack = getPickupItem();
            if (stack.is(UTags.Items.HORSE_SHOES)) {
                if (level() instanceof ServerLevel sw) {
                    stack.hurtAndBreak(1 + random.nextInt(10), sw, null, i -> {
                        playSound(USounds.Vanilla.ITEM_BREAK.value(), 1, 1);
                    });
                    if (!stack.isEmpty()) {
                        spawnAtLocation(sw, stack);
                    }
                    discard();
                }
            }
        }
        super.onHitEntity(hit);
    }

    @Override
    public DamageSources damageSources() {
        return new DamageSources(registryAccess()) {
            @Override
            public DamageSource arrow(AbstractArrow source, @Nullable Entity attacker) {
                return source(damageType, source, attacker);
            }
        };
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        BlockPos buttonPos = hit.getBlockPos().relative(hit.getDirection());
        BlockState state = level().getBlockState(buttonPos);

        @Nullable
        Player player = this.getOwner() instanceof Player p ? p : null;

        if (state.is(BlockTags.WOODEN_BUTTONS) && state.getBlock() instanceof ButtonBlock button) {
            button.press(state, level(), buttonPos, player);
        } else if (state.getBlock() instanceof LeverBlock lever) {
            lever.pull(state, level(), buttonPos, player);
        }

        BlockPos belowPos = buttonPos.below();
        BlockState below = level().getBlockState(belowPos);
        ItemStack stack = getItem();
        if (below.getBlock() instanceof HopperBlock) {
            BlockEntity e = level().getBlockEntity(belowPos);
            if (e instanceof Container inventory) {
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack slotStack = inventory.getItem(i);
                    if (slotStack.isEmpty()) {
                        inventory.setItem(i, stack);
                        discard();
                        break;
                    }

                    if (ItemStack.isSameItemSameComponents(slotStack, stack) && slotStack.getCount() < slotStack.getMaxStackSize()) {
                        slotStack.grow(1);
                        discard();
                        break;
                    }
                }
            }
        }

        if (getDeltaMovement().length() > 0.2F) {
            boolean ownerCanModify = !level().isClientSide() && Caster.of(getOwner()).filter(pony -> pony.canModifyAt(hit.getBlockPos())).isPresent();

            if (ownerCanModify && ((ServerLevel)level()).getGameRules().get(GameRules.MOB_GRIEFING)) {
                if ((!isBouncy() || level().getRandom().nextInt(200) == 0) && level().getBlockState(hit.getBlockPos()).is(UTags.Blocks.FRAGILE)) {
                    level().destroyBlock(hit.getBlockPos(), true);
                }
            }

            if (isBouncy()) {
                Direction.Axis side = hit.getDirection().getAxis();

                double randomisation = ((level().getRandom().nextFloat() - 0.5F) / 5);

                double inflectionAmount = randomisation + -0.4;
                double deflectionAmount = randomisation + 0.3;

                if (side == Direction.Axis.X) {
                    setDeltaMovement(getDeltaMovement().multiply(inflectionAmount, deflectionAmount, deflectionAmount));
                }

                if (side == Direction.Axis.Y) {
                    setDeltaMovement(getDeltaMovement().multiply(deflectionAmount, inflectionAmount, deflectionAmount));
                }

                if (side == Direction.Axis.Z) {
                    setDeltaMovement(getDeltaMovement().multiply(deflectionAmount, deflectionAmount, inflectionAmount));
                }

                push(
                        ((level().getRandom().nextFloat() - 0.5F) / 5),
                        ((level().getRandom().nextFloat() - 0.5F) / 5),
                        ((level().getRandom().nextFloat() - 0.5F) / 5)
                );
            } else {
                super.onHitBlock(hit);
            }
        } else {
            super.onHitBlock(hit);
        }

        setSoundEvent(state.getSoundType().getStepSound());
        gameEvent(GameEvent.STEP);

        if (!isBouncy()) {
            if (stack.is(UTags.Items.HORSE_SHOES)) {
                if (level() instanceof ServerLevel sw) {
                    stack.hurtAndBreak(1 + random.nextInt(10), sw, null, i -> {
                        playSound(USounds.Vanilla.ITEM_BREAK.value(), 1, 1);
                    });
                }
                if (stack.isEmpty()) {
                    discard();
                    return;
                }
            }

            level().playSound(null, this, getDefaultHitGroundSoundEvent(), SoundSource.BLOCKS, 0.6F, 1);
        } else {
            level().playSound(null, this, state.getSoundType().getStepSound(), SoundSource.BLOCKS, 1, 1);
        }
    }

    @Override
    public boolean mayBreak(ServerLevel world, BlockPos pos) {
        return !isBouncy() && super.mayBreak(world, pos);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        if (getItem().is(UTags.Items.HORSE_SHOES)) {
            return USounds.Vanilla.TRIDENT_HIT_GROUND;
        }
        return isBouncy() ? USounds.ITEM_MUFFIN_BOUNCE.value() : USounds.ITEM_ROCK_LAND;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        ItemStack stack = getItem();
        if (!stack.isEmpty()) {
            nbt.store("item", ItemStack.CODEC, stack);
        }
        nbt.putString("damageType", damageType.identifier().toString());
    }

    @Override
    public void readAdditionalSaveData(ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        nbt.read("item", ItemStack.CODEC).ifPresent(this::setPickupItemStack);
        nbt.read("damageType", ResourceKey.codec(Registries.DAMAGE_TYPE)).ifPresent(this::setDamageType);
    }
}
