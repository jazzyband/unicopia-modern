package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.mob.MimicEntity;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(RandomizableContainerBlockEntity.class)
abstract class MixinLootableContainerBlockEntity extends BaseContainerBlockEntity implements RandomizableContainer, MimicEntity.MimicGeneratable {
    @Nullable
    private ResourceKey<LootTable> mimicLootTable;
    private boolean allowMimics = true;
    private TriState isMimic = TriState.DEFAULT;

    @Shadow
    @Nullable
    protected ResourceKey<LootTable> lootTable;

    MixinLootableContainerBlockEntity() { super(null, null, null); }

    @Override
    public void readMimicAttributes(net.minecraft.world.level.storage.ValueInput nbt) {
        isMimic = nbt.read("mimic", com.mojang.serialization.Codec.BOOL).map(TriState::of).orElse(TriState.DEFAULT);
    }

    @Override
    public void writeMimicAttributes(net.minecraft.world.level.storage.ValueOutput nbt) {
        if (isMimic != TriState.DEFAULT) {
            nbt.putBoolean("mimic", isMimic.get());
        }
    }

    @Override
    public void configureMimic(@Nullable Player player) {
        if (player != null && allowMimics && lootTable != null) {
            mimicLootTable = lootTable;
        }
    }

    @Override
    public void setAllowMimics(boolean allowMimics) {
        this.allowMimics = allowMimics;
        setChanged();
    }

    @Override
    public void setMimic(boolean mimic) {
        isMimic = TriState.of(mimic);
        setChanged();
    }

    @Nullable
    @Override
    public MimicEntity triggerMimic(@Nullable Player player) {
        unpackLootTable(player);
        if (player != null && allowMimics) {
            if (isMimic == TriState.DEFAULT) {
                isMimic = TriState.of(MimicEntity.shouldConvert(player.level(), getBlockPos(), player, mimicLootTable));
            }

            if (isMimic.get()) {
                var mimic = MimicEntity.spawnFromChest(player.level(), getBlockPos());
                if (mimic != null) {
                    mimicLootTable = null;
                }

                return mimic;
            }

            mimicLootTable = null;
        }

        return null;
    }

    @Inject(
            method = "createMenu",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/block/entity/RandomizableContainerBlockEntity;unpackLootTable(Lnet/minecraft/world/entity/player/Player;)V",
                shift = Shift.AFTER
    ), cancellable = true)
    private void onCreateMenu(int syncId, Inventory playerInventory, Player player, CallbackInfoReturnable<AbstractContainerMenu> info) {
        @Nullable
        var mimic = triggerMimic(player);
        if (mimic != null) {
            info.setReturnValue(mimic.createScreenHandler(syncId, playerInventory, player));
        }
    }
}

@Mixin(RandomizableContainer.class)
interface MixinLootableInventory {
    @Inject(method = "unpackLootTable", at = @At("HEAD"))
    default void onGenerateLoot(@Nullable Player player, CallbackInfo info) {
        if (this instanceof MimicEntity.MimicGeneratable m) {
            m.configureMimic(player);
        }
    }

    @Inject(method = "tryLoadLootTable", at = @At("HEAD"))
    default void onReadLootTable(net.minecraft.world.level.storage.ValueInput nbt, CallbackInfoReturnable<Boolean> info) {
        if (this instanceof MimicEntity.MimicGeneratable m) {
            m.readMimicAttributes(nbt);
        }
    }

    @Inject(method = "trySaveLootTable", at = @At("HEAD"))
    default void onWriteLootTable(net.minecraft.world.level.storage.ValueOutput nbt, CallbackInfoReturnable<Boolean> info) {
        if (this instanceof MimicEntity.MimicGeneratable m) {
            m.writeMimicAttributes(nbt);
        }
    }
}
