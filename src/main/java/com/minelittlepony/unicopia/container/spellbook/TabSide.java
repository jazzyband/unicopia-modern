package com.minelittlepony.unicopia.container.spellbook;

import java.util.Locale;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;

public enum TabSide implements StringRepresentable {
    LEFT,
    RIGHT;

    public static final Codec<TabSide> CODEC = StringRepresentable.fromEnum(TabSide::values);
    public static final StreamCodec<ByteBuf, TabSide> PACKET_CODEC = PacketCodecUtils.ofEnum(TabSide.class);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
        return name;
    }
}