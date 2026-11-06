package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.sollace.fabwork.api.packets.Handled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Received by the server when a player changes their opened spellbook's state
 * Received by the client when another player changes the shared spellbook's state
 */
public record MsgSpellbookStateChanged<T extends Player> (
        int syncId,
        SpellbookState state
    ) implements Handled<T> {
    private static final StreamCodec<RegistryFriendlyByteBuf, MsgSpellbookStateChanged<?>> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MsgSpellbookStateChanged::syncId,
            SpellbookState.PACKET_CODEC, MsgSpellbookStateChanged::state,
            MsgSpellbookStateChanged::new
    );

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T extends Player> StreamCodec<RegistryFriendlyByteBuf, MsgSpellbookStateChanged<T>> packetCodec() {
        return (StreamCodec)PACKET_CODEC;
    }

    public static <T extends Player> MsgSpellbookStateChanged<T> create(SpellbookScreenHandler handler, SpellbookState state) {
        return new MsgSpellbookStateChanged<>(handler.containerId, state);
    }

    @Override
    public void handle(T sender) {
        if (sender.containerMenu.containerId != syncId) {
            return;
        }

        if (sender.containerMenu instanceof SpellbookScreenHandler spellbook) {
            spellbook.getSpellbookState().copyFrom(state);
            if (sender instanceof ServerPlayer) {
                spellbook.getSpellbookState().synchronize();
            }
        }
    }
}
