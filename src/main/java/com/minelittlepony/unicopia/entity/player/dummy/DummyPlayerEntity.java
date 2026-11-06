package com.minelittlepony.unicopia.entity.player.dummy;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.mojang.authlib.GameProfile;

public class DummyPlayerEntity extends Player implements Owned<Player>, Owned.Mutable<Player> {

    private Player owner;

    public DummyPlayerEntity(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public GameType gameMode() {
        return GameType.DEFAULT_MODE;
    }

    @Override
    public void onEquipItem(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        /*noop*/
    }

    @Override
    protected void onInsideBlock(BlockState state) {
        /*noop*/
    }

    @Override
    @Nullable
    public Player getMaster() {
        return owner;
    }

    @Override
    public void setMaster(Player owner) {
        this.owner = owner;
    }

    @Override
    public Optional<UUID> getMasterId() {
        return Optional.ofNullable(owner).map(Entity::getUUID);
    }

    @Override
    public boolean shouldShowName() {
        return !InteractionManager.getInstance().isClientPlayer(getMaster());
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
