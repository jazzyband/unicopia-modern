package com.minelittlepony.unicopia;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public interface WorldConvertable {
    /**
     * Gets the minecraft world
     */
    Level asWorld();

    /**
     * Gets the minecraft server world
     */
    default ServerLevel asServerWorld() {
        return (ServerLevel)asWorld();
    }

    /**
     * Returns true if we're executing on the client.
     */
    default boolean isClient() {
        return asWorld().isClientSide();
    }

    default <T> Holder<T> entryFor(ResourceKey<T> key) {
        return asWorld().registryAccess().lookupOrThrow(key.registryKey()).get(key.identifier()).orElseThrow();
    }
}
