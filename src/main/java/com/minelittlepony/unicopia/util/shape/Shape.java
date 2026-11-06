package com.minelittlepony.unicopia.util.shape;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 *
 *Interface for a 3d shape, used for spawning particles in a designated area (or anything else you need shapes for).
 */
public interface Shape extends PointGenerator {
    /**
     * Get the volume of space filled by this shape, or the surface area if hollow.
     *
     * @return double volume
     */
    double getVolume();

    /**
     * Gets the lower bounds of the region occupied by this shape.
     */
    Vec3 getLowerBound();

    /**
     * Gets the upper bound of the region occupied by this shape.
     */
    Vec3 getUpperBound();

    /**
     * Checks if the given point is on the edge, or if not hollow the inside, of this shape.
     */
    boolean isPointInside(Vec3 point);

    /**
     * Returns a stream of all block positions that fit inside this shape.
     */
    default Stream<BlockPos> getBlockPositions() {
        return BlockPos.betweenClosedStream(
            BlockPos.containing(getLowerBound()),
            BlockPos.containing(getUpperBound())
        ).filter(pos -> isPointInside(Vec3.atCenterOf(pos)));
    }

    /**
     * Returns a endless stream of random block positions that fit inside this shape.
     */
    default Stream<BlockPos> randomBlockPositions(RandomSource rand) {
        return randomPoints(rand).map(BlockPos::containing).distinct();
    }

    /**
     * Returns a sequence of random points dealt out to uniformly fill this shape's area.
     */
    default Stream<Vec3> randomPoints(RandomSource rand) {
        return randomPoints((int)getVolume(), rand);
    }

    /**
     * Returns a new shape with after applying additional rotation.
     */
    @Override
    default Shape rotate(float pitch, float yaw) {
        return pitch == 0 && yaw == 0 ? this : new RotatedPointGenerator(this, pitch, yaw);
    }

    @Override
    default Shape translate(Vec3i offset) {
        return offset.equals(Vec3i.ZERO) ? this : translate(Vec3.atLowerCornerOf(offset));
    }

    @Override
    default Shape translate(Vec3 offset) {
        return offset.equals(Vec3.ZERO) ? this : new TranslatedPointGenerator(this, offset);
    }
}
