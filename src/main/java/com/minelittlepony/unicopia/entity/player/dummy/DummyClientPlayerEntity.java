package com.minelittlepony.unicopia.entity.player.dummy;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.mojang.authlib.GameProfile;

public class DummyClientPlayerEntity extends AbstractClientPlayer implements Owned<Player>, Owned.Mutable<Player> {

    private PlayerInfo playerInfo;

    private Player owner;

    public DummyClientPlayerEntity(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public boolean isSpectator() {
        return getPlayerInfo().getGameMode() == GameType.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        return getPlayerInfo().getGameMode() == GameType.CREATIVE;
    }

    @Override
    @NotNull
    protected PlayerInfo getPlayerInfo() {
        if (playerInfo == null) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();

            playerInfo = connection.getPlayerInfo(getGameProfile().id());

            if (playerInfo == null) {
                playerInfo = new PlayerInfo(getGameProfile(), false);
            }
        }

        return playerInfo;
    }

    @Override
    public void onEquipItem(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        /*noop*/
    }

    @Override
    public boolean shouldShowName() {
        return !InteractionManager.getInstance().isClientPlayer(getMaster());
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart modelPart) {
        return owner == null ? super.isModelPartShown(modelPart) : owner.isModelPartShown(modelPart);
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
}
