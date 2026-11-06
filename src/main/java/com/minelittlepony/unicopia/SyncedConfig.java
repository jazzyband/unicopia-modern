package com.minelittlepony.unicopia;

import java.util.Set;
import net.minecraft.network.codec.StreamCodec;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;

import io.netty.buffer.ByteBuf;

public record SyncedConfig (
    Set<String> wantItNeedItExcludeList,
    Set<String> dimensionsWithoutAtmosphere) {
    public static final StreamCodec<ByteBuf, SyncedConfig> PACKET_CODEC = StreamCodec.composite(
            PacketCodecUtils.STRING_SET, SyncedConfig::wantItNeedItExcludeList,
            PacketCodecUtils.STRING_SET, SyncedConfig::dimensionsWithoutAtmosphere,
            SyncedConfig::new
    );
}
