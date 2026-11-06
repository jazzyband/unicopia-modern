package com.minelittlepony.unicopia.block.jar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.block.ItemJarBlock.JarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.TileData;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.mixin.MixinEntityBucketItem;
import com.minelittlepony.unicopia.util.FluidHelper;
import com.minelittlepony.unicopia.util.TypedActionResult;
import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import com.minelittlepony.unicopia.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public record ItemsJarContents (
        TileData tile,
        List<ItemStack> stacks
    ) implements JarContents, WorldlyContainer {
    private static final int MAX_SIZE = 16;
    private static final int[] SLOTS = IntStream.range(0, MAX_SIZE).toArray();
    private static final Codec<List<ItemStack>> STACKS_CODEC = ItemStack.CODEC.listOf(0, MAX_SIZE);

    public ItemsJarContents(TileData tile) {
        this(tile, new ArrayList<>(MAX_SIZE));
    }

    public ItemsJarContents(TileData tile, CompoundTag compound, Provider lookup) {
        this(tile, new ArrayList<>(compound.read("items", STACKS_CODEC, lookup.createSerializationContext(NbtOps.INSTANCE)).orElse(List.of())));
    }

    @Override
    public TypedActionResult<JarContents> interact(Player player, InteractionHand hand) {
        ItemStack handStack = player.getItemInHand(hand);

        if (handStack.isEmpty()) {
            if (stacks.isEmpty()) {
                return TypedActionResult.fail(this);
            }
            Block.popResource(tile.getLevel(), tile.getBlockPos(), stacks.remove(0));
            setChanged();
            return TypedActionResult.success(this);
        }

        if (stacks.isEmpty()) {
            if (handStack.getItem() instanceof MixinEntityBucketItem bucket) {
                consumeAndSwap(player, hand, Items.BUCKET.getDefaultInstance());
                player.playSound(bucket.getEmptySound(), 1, 1);
                setChanged();
                return TypedActionResult.success(new EntityJarContents(tile, bucket.getType()));
            }

            Tuple<Long, FluidVariant> fluid = FluidHelper.extract(handStack, player, hand).orElse(null);
            if (fluid != null) {
                fluid.getB().getFluid().getPickupSound().ifPresent(sound -> player.playSound(sound, 1, 1));
                setChanged();
                return TypedActionResult.success(new FluidOnlyJarContents(tile, fluid.getA(), fluid.getB()));
            }

            if (handStack.is(Items.MILK_BUCKET)) {
                consumeAndSwap(player, hand, Items.BUCKET.getDefaultInstance());
                player.playSound(USounds.Vanilla.BUCKET_EMPTY, 1, 1);
                setChanged();
                return TypedActionResult.success(new FakeFluidJarContents(tile, "milk", 0xFFFFFFFF, Items.BUCKET, Items.MILK_BUCKET));
            }

            if (handStack.is(Items.POWDER_SNOW_BUCKET)) {
                consumeAndSwap(player, hand, Items.BUCKET.getDefaultInstance());
                player.playSound(USounds.Vanilla.BUCKET_EMPTY_POWDER_SNOW, 1, 1);
                setChanged();
                return TypedActionResult.success(new FakeFluidJarContents(tile, "powder_snow", 0xFFFFFFFF, Items.BUCKET, Items.POWDER_SNOW_BUCKET));
            }

            if (handStack.is(UItems.LOVE_BUCKET)) {
                consumeAndSwap(player, hand, Items.BUCKET.getDefaultInstance());
                player.playSound(USounds.Vanilla.BUCKET_EMPTY, 1, 1);
                setChanged();
                return TypedActionResult.success(new FakeFluidJarContents(tile, "love", 0xFF3030, Items.BUCKET, UItems.LOVE_BUCKET));
            }

            if (handStack.is(UItems.JUICE)) {
                consumeAndSwap(player, hand, Items.GLASS_BOTTLE.getDefaultInstance());
                player.playSound(USounds.Vanilla.BUCKET_EMPTY, 1, 1);
                setChanged();
                return TypedActionResult.success(new FakeFluidJarContents(tile, "apple_juice", 0x30FF30, Items.GLASS_BOTTLE, UItems.JUICE));
            }
        }

        if (stacks.size() >= getContainerSize()) {
            return TypedActionResult.fail(this);
        }
        stacks.add(player.isCreative() ? handStack.copyWithCount(1) : handStack.split(1));
        setChanged();

        return TypedActionResult.success(this);
    }

    @Override
    public void onDestroyed() {
        stacks.forEach(stack -> Block.popResource(tile.getLevel(), tile.getBlockPos(), stack));
    }

    @Override
    public int getContainerSize() {
        return MAX_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot < 0 || slot >= stacks.size() ? ItemStack.EMPTY : stacks.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= stacks.size()) {
            return ItemStack.EMPTY;
        }

        try {
            ItemStack stack = stacks.get(slot);
            ItemStack removed = stack.split(1);
            if (stack.isEmpty()) {
                stacks.remove(slot);
            }
            return removed;
        } finally {
            setChanged();
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            return ItemStack.EMPTY;
        }

        try {
            return stacks.remove(slot);
        } finally {
            setChanged();
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= stacks.size()) {
            stacks.add(stack);
        } else {
            stacks.set(slot, stack);
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        stacks.clear();
        setChanged();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot >= 0 && slot < getContainerSize() && slot >= stacks.size();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot >= 0 && slot < getContainerSize() && slot < stacks.size();
    }

    @Override
    public CompoundTag toNBT(CompoundTag compound, Provider lookup) {
        compound.store("items", STACKS_CODEC, lookup.createSerializationContext(NbtOps.INSTANCE), stacks);
        return compound;
    }

    @Override
    public void setChanged() {
        tile.setChanged();
    }
}
