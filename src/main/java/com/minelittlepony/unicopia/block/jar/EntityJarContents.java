package com.minelittlepony.unicopia.block.jar;

import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.block.ItemJarBlock.FluidJarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.JarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.TileData;
import com.minelittlepony.unicopia.util.TypedActionResult;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public record EntityJarContents (
        TileData tile,
        @Nullable EntityType<?> entityType,
        Supplier<@Nullable Entity> entity
) implements FluidJarContents {
    public EntityJarContents(TileData tile, CompoundTag compound) {
        this(tile, compound.read("entity", BuiltInRegistries.ENTITY_TYPE.byNameCodec()).orElse(null));
    }

    public EntityJarContents(TileData tile) {
        this(tile, (EntityType<?>)null);
    }

    public EntityJarContents(TileData tile, EntityType<?> entityType) {
        this(tile, entityType, Suppliers.memoize(() -> {
            return entityType == null ? null : entityType.create(tile.getLevel(), EntitySpawnReason.LOAD);
        }));
    }

    @Override
    public TypedActionResult<JarContents> interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.BUCKET)) {
            if (entity().get() instanceof Bucketable bucketable) {
                consumeAndSwap(player, hand, bucketable.getBucketItemStack());
                player.playSound(bucketable.getPickupSound(), 1, 1);
            }
            tile.setChanged();
            return TypedActionResult.success(new ItemsJarContents(tile));
        }
        return TypedActionResult.pass(this);
    }

    @Override
    public void onDestroyed() {
        tile.getLevel().setBlockAndUpdate(tile.getBlockPos(), Blocks.WATER.defaultBlockState());
        Entity entity = entity().get();
        if (entity != null) {
            entity.snapTo(Vec3.atCenterOf(tile.getBlockPos()));
            tile.getLevel().addFreshEntity(entity);
        }
    }

    @Override
    public CompoundTag toNBT(CompoundTag compound, Provider lookup) {
        compound.putString("entity", EntityType.getKey(entityType).toString());
        return compound;
    }

    @Override
    public FluidVariant fluid() {
        return FluidVariant.of(Fluids.WATER);
    }
}