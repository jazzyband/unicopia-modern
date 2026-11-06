package com.minelittlepony.unicopia.ability.data;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface Hit {
    Optional<Hit> INSTANCE = Optional.of(new Hit() {});
    StreamCodec<FriendlyByteBuf, Hit> CODEC = StreamCodec.unit(INSTANCE.get());

    static Optional<Hit> of(boolean value) {
        return value ? INSTANCE : Optional.empty();
    }
}