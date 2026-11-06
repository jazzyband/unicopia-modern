package com.minelittlepony.unicopia;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Interface for things that can be owned by an entity.
 * <p>
 * Ownership is retained so long as the owner is still active. If the owner leaves or dies, the link is broken.
 *
 * @param <E> The type of object that owns us.
 */
public interface EntityConvertable<E extends Entity> extends WorldConvertable {
    E asEntity();

    /**
     * Gets the center position where this caster is located.
     */
    default BlockPos getOrigin() {
        return asEntity().blockPosition();
    }
    /**
     * Gets the center position where this caster is located.
     */
    default Vec3 getOriginVector() {
        return asEntity().position().add(0, asEntity().getBbHeight() * 0.5F, 0);
    }

    @Override
    default Level asWorld() {
        return asEntity().level();
    }

    @SuppressWarnings("deprecation")
    default boolean isPosLoaded(BlockPos pos) {
        return asWorld().hasChunkAt(pos);
    }
}
