package com.minelittlepony.unicopia.network.track;

import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public interface TrackableObject<T extends TrackableObject<T>> {
    Status getStatus();

    void read(RegistryFriendlyByteBuf buffer);

    void write(RegistryFriendlyByteBuf buffer);

    default Optional<? extends ByteBuf> write(Status status, RegistryAccess lookup) {
        if (status == Status.NEW || status == Status.UPDATED) {
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), lookup);
            write(buffer);
            return Optional.of(buffer);
        }
        return Optional.empty();
    }

    void discard(boolean immediate);

    void copyTo(T destination);

    public enum Status {
        DEFAULT,
        NEW,
        UPDATED,
        REMOVED
    }
}
