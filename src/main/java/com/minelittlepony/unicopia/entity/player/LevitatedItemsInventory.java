package com.minelittlepony.unicopia.entity.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;
import com.minelittlepony.unicopia.item.ForageableItem;
import com.minelittlepony.unicopia.util.Copyable;
import com.minelittlepony.unicopia.util.Tickable;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class LevitatedItemsInventory implements Copyable<LevitatedItemsInventory>, NbtSerialisable, Tickable {

    private final Pony player;

    private final Int2ObjectMap<LevitatingItemEntity> stacks = new Int2ObjectAVLTreeMap<>();
    private final List<LevitatingItemEntity> pending = new ArrayList<>();
    private int nextSlot = 0;

    private final Map<BlockPos, BlockBreakingRecord> blockBreakingRecords = new HashMap<>();

    public LevitatedItemsInventory(Pony player) {
        this.player = player;
    }

    public void onEntitySpawned(LevitatingItemEntity entity) {
        stacks.put(entity.getSlot(), entity);
    }

    public void onEntityDespawned(LevitatingItemEntity entity) {
        if (stacks.containsKey(entity.getSlot())) {
            stacks.remove(entity.getSlot());
        }
    }

    public boolean addStack(ItemStack stack) {
        LevitatingItemEntity entity = new LevitatingItemEntity(nextSlot++, player.asEntity(), stack);
        onEntitySpawned(entity);
        player.asWorld().addFreshEntity(entity);
        player.playSound(SoundEvents.ITEM_PICKUP, 1);
        return true;
    }

    public boolean addPassenger(Entity passenger) {
        double yOffset = 0.1;
        if (passenger.getRootVehicle() instanceof LevitatingItemEntity root) {
            boolean isOnlyPassenger = root.getPassengers().size() == 1 && root.getPassengers().get(0) == passenger;
            passenger.stopRiding();
            passenger.setPos(root.position());
            yOffset = 0;
            if (root.getPassengers().isEmpty()) {
                discardOrKill(root);
            }
            if (root.getMaster() == player.asEntity() && isOnlyPassenger) {
                player.playSound(SoundEvents.ITEM_PICKUP, 1);
                return true;
            }
        }
        LevitatingItemEntity entity = new LevitatingItemEntity(nextSlot++, player.asEntity(), ItemStack.EMPTY);
        entity.setPos(passenger.position());
        entity.setHoldingPosition(passenger.position().add(0, yOffset, 0));
        if (passenger instanceof ItemEntity i) {
            entity.setStack(i.getItem());
            i.discard();
            return addStack(i.getItem());
        } else {
            onEntitySpawned(entity);
            passenger.startRiding(entity, true, true);
            player.asWorld().addFreshEntity(entity);
        }

        player.playSound(SoundEvents.ITEM_PICKUP, 1);
        return true;
    }

    public void dropEverything() {
        List<LevitatingItemEntity> copy = new ArrayList<>(stacks.values());
        if (!copy.isEmpty()) {
            player.playSound(SoundEvents.ITEM_PICKUP, 1);
        }
        stacks.clear();
        nextSlot = 0;
        copy.forEach(this::discardOrKill);
    }

    private void discardOrKill(Entity entity) {
        if (entity.level() instanceof ServerLevel sw) {
            entity.kill(sw);
        } else {
            entity.discard();
        }
    }

    public InteractionResult interact(@Nullable BlockHitResult hit) {
        for (var i : stacks.values()) {
            ItemStack stack = i.getStack();
            if (player.asEntity().getCooldowns().isOnCooldown(stack)) {
                continue;
            }

            if (ForageableItem.use(player.asEntity(), stack, player.asWorld(), InteractionHand.MAIN_HAND, hit).consumesAction()) {
                return InteractionResult.SUCCESS;
            }

            var result = stack.useOn(new UseOnContext(player.asWorld(), player.asEntity(), InteractionHand.MAIN_HAND, stack, hit));
            if (result.consumesAction()) {
                if (player.asEntity() instanceof ServerPlayer spe) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(spe, hit.getBlockPos(), stack);
                }
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    public InteractionResult interact(Entity entity, @Nullable EntityHitResult hit) {
        if (!(entity instanceof LivingEntity l)) {
            return InteractionResult.PASS;
        }
        for (var i : stacks.values()) {
            ItemStack stack = i.getStack();
            if (player.asEntity().getCooldowns().isOnCooldown(stack)) {
                continue;
            }

            var result = stack.interactLivingEntity(player.asEntity(), l, InteractionHand.MAIN_HAND);
            if (result.consumesAction()) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    public InteractionResult attack(Entity entity, @Nullable EntityHitResult hit) {
        double playerAttackDamage = player.asEntity().getAttributeValue(Attributes.ATTACK_DAMAGE);
        @Nullable
        LevitatingItemEntity chosenAttacker = null;
        double chosenAttackerDamage = playerAttackDamage;

        for (var i : stacks.values()) {
            if (player.asEntity().getCooldowns().isOnCooldown(i.getStack())
                    || i.getMiningPos().isPresent()
                    || !i.canChangePositionHoldingFreely()
                    || i.isSwinging()) {
                continue;
            }

            double damage = i.getAttackDamage(player.asEntity());
            if (damage > chosenAttackerDamage) {
                chosenAttacker = i;
                chosenAttackerDamage = damage;
            }

            player.asEntity().attack(entity);
        }

        if (chosenAttacker != null) {
            chosenAttacker.swingAt(entity.blockPosition().offset(0, (int)entity.getBbHeight(), 0));
            ItemStack oldStack = player.asEntity().getItemInHand(InteractionHand.MAIN_HAND);
            try {
                player.asEntity().setItemInHand(InteractionHand.MAIN_HAND, chosenAttacker.getStack());
                player.asEntity().attack(entity);
            } finally {
                chosenAttacker.setStack(player.asEntity().getItemInHand(InteractionHand.MAIN_HAND));
                player.asEntity().setItemInHand(InteractionHand.MAIN_HAND, oldStack);
            }
        }

        return InteractionResult.PASS;
    }

    public boolean startMining(BlockState state, BlockPos pos, Direction direction) {
        blockBreakingRecords.values().removeIf(BlockBreakingRecord::tryDiscard);
        BlockBreakingRecord record = blockBreakingRecords.computeIfAbsent(pos, p -> new BlockBreakingRecord(p, state));
        for (var i : stacks.values()) {
            if (i.startMining(player.asEntity(), record, direction)) {
                return true;
            }
        }
        return false;
    }

    public void stopMining(BlockPos pos) {
        for (var i : stacks.values()) {
            i.stopMining(pos);
        }
    }

    @Override
    public void tick() {
        if (!player.isClient()) {
            if (!Abilities.TELEKINESIS.canUse(player.getCompositeRace())) {
                dropEverything();
            }

            List<LevitatingItemEntity> copy = new ArrayList<>(pending);
            pending.clear();
            copy.forEach(player.asWorld()::addFreshEntity);
        }

        blockBreakingRecords.values().removeIf(BlockBreakingRecord::tryDiscard);
        stacks.values().removeIf(Entity::isRemoved);

        if (!player.isClient()) {
            float cost = 0;
            for (LevitatingItemEntity a : stacks.values()) {
                cost += 0.01F * a.getStack().getCount();
                cost += 0.1F * a.getPassengers().size();
                for (LevitatingItemEntity b : stacks.values()) {
                    if (a != b) {
                        if (a.getRelativePosition().distanceTo(b.getRelativePosition()) < 0.8) {
                            Vec3 delta = a.getRelativePosition().subtract(b.getRelativePosition()).scale(0.5);
                            a.setPolarPositionOffset(a.getRelativePosition().add(delta));
                            b.setPolarPositionOffset(b.getRelativePosition().subtract(delta));
                        }
                    }
                }
            }

            player.subtractEnergyCost(cost);
        }

    }

    @Override
    public void copyFrom(LevitatedItemsInventory other, boolean alive) {
        if (alive) {
            for (var stack : other.stacks.values()) {
                addStack(stack.getStack());
                stack.discard();
            }
        }
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        ListTag stacks = new ListTag();
        for (var stack : this.stacks.values()) {
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, lookup);
            stack.saveWithoutId(output);
            stacks.add(output.buildResult());
        }
        compound.put("stacks", stacks);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        ListTag stacks = compound.getListOrEmpty("stacks");
        this.stacks.clear();
        for (int i = 0; i < stacks.size(); i++) {
            LevitatingItemEntity stack = new LevitatingItemEntity(i, player.asEntity(), ItemStack.EMPTY);
            stack.load(TagValueInput.create(ProblemReporter.DISCARDING, lookup, stacks.getCompoundOrEmpty(i)));
            stack.setSlot(i);
            onEntitySpawned(stack);
            if (!player.isClient()) {
                this.pending.add(stack);
            }
        }
        nextSlot = this.stacks.size();
    }

    public class BlockBreakingRecord {
        public final BlockPos pos;
        public final BlockState state;

        public float breakingProgress;
        public int breakingStage;

        public final Set<LevitatingItemEntity> claimants = new HashSet<>();

        public BlockBreakingRecord(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }

        public boolean tryDiscard() {
            if (breakingProgress >= 1F || player.asWorld().getBlockState(pos) != state || state.isAir() || claimants.isEmpty()) {
                new HashSet<>(claimants).forEach(claimant -> claimant.stopMining(pos));
                claimants.clear();
                return true;
            }

            return false;
        }
    }
}
