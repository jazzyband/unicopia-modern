package com.minelittlepony.unicopia.ability.data;

import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import io.netty.buffer.ByteBuf;

public record Numeric (int type) implements Hit {
    private static final Interner<Numeric> INTERNER = Interners.newWeakInterner();
    public static final StreamCodec<ByteBuf, Numeric> CODEC = ByteBufCodecs.INT.map(Numeric::new, Numeric::type);

    public static Optional<Numeric> of(int type) {
        return Optional.of(valueOf(type));
    }

    public static Numeric valueOf(int type) {
        return INTERNER.intern(new Numeric(type));
    }
}