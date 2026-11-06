package com.minelittlepony.unicopia.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MsgSkyAngle (
        float tangentalSkyAngle
    ) {
    public static final StreamCodec<ByteBuf, MsgSkyAngle> PACKET_CODEC = ByteBufCodecs.FLOAT.map(MsgSkyAngle::new, MsgSkyAngle::tangentalSkyAngle);
}
