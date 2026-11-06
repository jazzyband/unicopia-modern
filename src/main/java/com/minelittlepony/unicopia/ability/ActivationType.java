package com.minelittlepony.unicopia.ability;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public enum ActivationType {
    NONE,
    TAP,
    DOUBLE_TAP,
    TRIPLE_TAP;

    private static final ActivationType[] VALUES = values();
    public static final StreamCodec<ByteBuf, ActivationType> PACKET_CODEC = ByteBufCodecs.idMapper(i -> VALUES[i], ActivationType::ordinal);

    public ActivationType getNext() {
        return VALUES[Math.min(VALUES.length - 1, ordinal() + 1)];
    }

    public int getTapCount() {
        return ordinal();
    }

    public boolean isResult() {
        return this != NONE;
    }

    @Deprecated
    public static ActivationType of(int id) {
        return VALUES[Mth.clamp(id, 0, VALUES.length)];
    }
}
