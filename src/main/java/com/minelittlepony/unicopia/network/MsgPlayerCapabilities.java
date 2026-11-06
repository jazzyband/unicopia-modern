package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

/**
 * Sent to the client to update various data pertaining to a particular player.
 * <p>
 * Also used by the server to notify a race change.
 */
public class MsgPlayerCapabilities implements Handled<Player> {
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgPlayerCapabilities> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, i -> i.playerId,
            ByteBufCodecs.COMPOUND_TAG, i -> i.compoundTag,
            ByteBufCodecs.BOOL, i -> i.initial,
            MsgPlayerCapabilities::new
    );

    protected final int playerId;

    private final CompoundTag compoundTag;
    private final boolean initial;

    MsgPlayerCapabilities(int playerId, CompoundTag compoundTag, boolean initial) {
        this.playerId = playerId;
        this.compoundTag = compoundTag;
        this.initial = initial;
    }

    public MsgPlayerCapabilities(Pony player, boolean initial) {
        playerId = player.asEntity().getId();
        compoundTag = new CompoundTag();
        this.initial = initial;
        if (initial) {
            player.toNBT(compoundTag, player.asWorld().registryAccess());
        } else {
            player.toSyncronisedNbt(compoundTag, player.asWorld().registryAccess());
        }
    }

    @Override
    public void handle(Player sender) {
        Pony player = Pony.of(sender.level().getEntity(playerId)).orElse(null);
        if (player != null) {
            if (initial) {
                player.fromNBT(compoundTag, sender.level().registryAccess());
            } else {
                player.fromSynchronizedNbt(compoundTag, sender.level().registryAccess());
            }
        }
    }
}
