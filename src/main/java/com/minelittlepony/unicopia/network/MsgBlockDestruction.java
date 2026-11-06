package com.minelittlepony.unicopia.network;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Sent by the server to update block destruction progress on the client.
 */
public record MsgBlockDestruction(Long2ObjectMap<Float> destructions) {
    public static final StreamCodec<ByteBuf, MsgBlockDestruction> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(Long2ObjectOpenHashMap::new, ByteBufCodecs.VAR_LONG, ByteBufCodecs.FLOAT), MsgBlockDestruction::destructions,
            MsgBlockDestruction::new
    );
}
