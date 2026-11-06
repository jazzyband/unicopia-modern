package com.minelittlepony.unicopia.block.jar;

import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.block.ItemJarBlock.JarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.TileData;
import com.minelittlepony.unicopia.util.FluidHelper;
import com.minelittlepony.unicopia.util.TypedActionResult;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

public record FakeFluidJarContents (
        TileData tile,
        String fluid,
        int color,
        Item empty,
        Item filled
) implements JarContents {
    public FakeFluidJarContents(TileData tile, CompoundTag compound) {
        this(tile, compound.getStringOr("fluid", ""), compound.getIntOr("color", 0),
                compound.read("empty", BuiltInRegistries.ITEM.byNameCodec()).orElse(Items.AIR),
                compound.read("filled", BuiltInRegistries.ITEM.byNameCodec()).orElse(Items.AIR));
    }

    @Override
    public TypedActionResult<JarContents> interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        tile.setChanged();
        return getRealFluid().map(FluidVariant::of).<TypedActionResult<JarContents>>map(fluid -> {
            long remainder = FluidHelper.deposit(stack, player, hand, fluid, FluidConstants.BUCKET);
            fluid.getFluid().getPickupSound().ifPresent(sound -> player.playSound(sound, 1, 1));
            if (remainder > 0) {
                return TypedActionResult.success(new FluidOnlyJarContents(tile, remainder, fluid));
            }
            return TypedActionResult.success(new ItemsJarContents(tile));
        }).orElseGet(() -> {
            if (!stack.is(empty)) {
                return TypedActionResult.pass(this);
            }
            consumeAndSwap(player, hand, filled.getDefaultInstance());
            player.playSound("powder_snow".equalsIgnoreCase(fluid) ? USounds.Vanilla.BUCKET_FILL_POWDER_SNOW : USounds.Vanilla.BUCKET_FILL, 1, 1);
            return TypedActionResult.success(new ItemsJarContents(tile));
        });
    }

    @Override
    public void onDestroyed() {
        getRealFluid().ifPresent(fluid -> {
            tile.getLevel().setBlockAndUpdate(tile.getBlockPos(), FluidHelper.getFullFluidState(FluidVariant.of(fluid)).createLegacyBlock());
        });
    }

    @Override
    public CompoundTag toNBT(CompoundTag compound, Provider lookup) {
        compound.putString("fluid", fluid);
        compound.putInt("color", color);
        compound.putString("empty", BuiltInRegistries.ITEM.getKey(empty).toString());
        compound.putString("filled", BuiltInRegistries.ITEM.getKey(filled).toString());
        return compound;
    }

    private Optional<Fluid> getRealFluid() {
        return BuiltInRegistries.FLUID.keySet().stream()
                .filter(id -> id.getPath().equalsIgnoreCase(fluid))
                .findFirst()
                .map(BuiltInRegistries.FLUID::getValue);
    }
}