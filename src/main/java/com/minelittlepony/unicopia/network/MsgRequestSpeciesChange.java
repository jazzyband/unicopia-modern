package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.sollace.fabwork.api.packets.Handled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sent to the server when a client wants to request a species change.
 * <p>
 * The server responds back with the accepted capabilities and the race the client should use (if the preferred was not permitted)
 */
public record MsgRequestSpeciesChange (
        boolean force,
        Race newRace
    ) implements Handled<ServerPlayer> {
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgRequestSpeciesChange> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MsgRequestSpeciesChange::force,
            ByteBufCodecs.registry(Race.REGISTRY_KEY), MsgRequestSpeciesChange::newRace,
            MsgRequestSpeciesChange::new
    );

    @Override
    public void handle(ServerPlayer sender) {
        Pony player = Pony.of(sender);

        if (force || player.getSpecies().isUnset()) {
            boolean permitted = newRace.isPermitted(sender);
            player.setSpecies(permitted ? newRace : UnicopiaWorldProperties.forWorld((ServerLevel)player.asWorld()).getDefaultRace());
            if (!permitted) {
                sender.sendSystemMessage(Component.translatable("respawn.reason.illegal_race", newRace.getDisplayName()), false);
            }

            if (force) {
                if (((ServerLevel)sender.level()).getGameRules().get(UGameRules.ANNOUNCE_TRIBE_JOINS)) {
                    Component message = Component.translatable("respawn.reason.joined_new_tribe",
                            sender.getDisplayName(),
                            player.getSpecies().getDisplayName(), player.getSpecies().getAltDisplayName());
                    sender.level().players().forEach(p -> {
                        ((ServerPlayer)p).sendSystemMessage(message, false);
                    });
                }

                player.forceRespawnOnRaceChange();
            }
        }

        player.sendUpdatePacket();
    }
}
