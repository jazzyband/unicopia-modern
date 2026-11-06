package com.minelittlepony.unicopia.container.spellbook;

import java.util.Locale;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;

public enum Flow implements StringRepresentable {
    NONE, LEFT, RIGHT;

    public static final Codec<Flow> CODEC = StringRepresentable.fromEnum(Flow::values);
    public static final StreamCodec<ByteBuf, Flow> PACKET_CODEC = PacketCodecUtils.ofEnum(Flow.class);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
        return name;
    }
}