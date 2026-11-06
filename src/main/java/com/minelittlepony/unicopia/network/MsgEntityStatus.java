package com.minelittlepony.unicopia.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MsgEntityStatus(int entityId, int status) {
    public static final int USE_TOTEM_OF_DYING = 1;

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgEntityStatus> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MsgEntityStatus::entityId,
            ByteBufCodecs.INT, MsgEntityStatus::status,
            MsgEntityStatus::new
    );
}
