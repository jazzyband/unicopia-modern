package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.SyncedConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record MsgConfigurationChange(SyncedConfig config) {
    public static final StreamCodec<ByteBuf, MsgConfigurationChange> PACKET_CODEC = SyncedConfig.PACKET_CODEC.map(MsgConfigurationChange::new, MsgConfigurationChange::config);
}
