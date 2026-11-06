package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record MsgPlayerFlightControlsInput (
        boolean ascending,
        boolean descending
    ) implements Handled<ServerPlayer> {
    public static final StreamCodec<ByteBuf, MsgPlayerFlightControlsInput> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MsgPlayerFlightControlsInput::ascending,
            ByteBufCodecs.BOOL, MsgPlayerFlightControlsInput::descending,
            MsgPlayerFlightControlsInput::new
    );

    public MsgPlayerFlightControlsInput(Pony pony) {
        this(pony.getJumpingHeuristic().getState(), pony.asEntity().isShiftKeyDown());
    }

    @Override
    public void handle(ServerPlayer sender) {
        sender.setShiftKeyDown(descending);
        sender.setJumping(ascending);
    }
}
