package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record MsgZapAppleStage (
        ZapAppleStageStore.Stage stage
    ) {
    public static final StreamCodec<ByteBuf, MsgZapAppleStage> PACKET_CODEC = ZapAppleStageStore.Stage.PACKET_CODEC.map(MsgZapAppleStage::new, MsgZapAppleStage::stage);
}
