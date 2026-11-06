package com.minelittlepony.unicopia.block.jar;

import com.minelittlepony.unicopia.block.ItemJarBlock.FluidJarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.JarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.TileData;
import com.minelittlepony.unicopia.util.FluidHelper;
import com.minelittlepony.unicopia.util.TypedActionResult;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record FluidOnlyJarContents (
        TileData tile,
        long amount,
        FluidVariant fluid
) implements FluidJarContents {

    public FluidOnlyJarContents(TileData tile, CompoundTag compound, Provider lookup) {
        this(tile, compound.getLongOr("amount", 0), compound.read("fluid", FluidVariant.CODEC, lookup.createSerializationContext(NbtOps.INSTANCE)).orElse(FluidVariant.blank()));
    }

    @Override
    public TypedActionResult<JarContents> interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ConventionalItemTags.EMPTY_BUCKETS)) {
            long remainder = FluidHelper.deposit(stack, player, hand, fluid, amount);
            tile.setChanged();
            fluid.getFluid().getPickupSound().ifPresent(sound -> player.playSound(sound, 1, 1));
            if (remainder > 0) {
                return TypedActionResult.success(new FluidOnlyJarContents(tile, remainder, fluid));
            }
            return TypedActionResult.success(new ItemsJarContents(tile));
        }
        return TypedActionResult.pass(this);
    }

    @Override
    public void onDestroyed() {
        if (amount >= FluidConstants.BUCKET) {
            tile.getLevel().setBlockAndUpdate(tile.getBlockPos(), FluidHelper.getFullFluidState(fluid).createLegacyBlock());
        }
    }

    @Override
    public CompoundTag toNBT(CompoundTag compound, Provider lookup) {
        compound.store("fluid", FluidVariant.CODEC, lookup.createSerializationContext(NbtOps.INSTANCE), fluid);
        compound.putLong("amount", amount);
        return compound;
    }
}