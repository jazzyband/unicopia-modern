package com.minelittlepony.unicopia.util.shape;

import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 *
 *Interface for a 3d shape, used for spawning particles in a designated area (or anything else you need shapes for).
 */
public interface PointGenerator {
    /**
     * Computes a random coordinate that falls within this shape's designated area.
     */
    Vec3 computePoint(RandomSource rand);

    /**
     * Returns a sequence of N random points.
     */
    default Stream<Vec3> randomPoints(int n, RandomSource rand) {
        return StreamSupport.stream(new AbstractSpliterator<Vec3>(n, Spliterator.SIZED) {
            private int index = n;

            @Override
            public boolean tryAdvance(Consumer<? super Vec3> consumer) {
                if (--index >= 0) {
                    consumer.accept(computePoint(rand));
                    return true;
                }

                return false;
            }
        }, false);
    }

    /**
     * Returns a new shape with after applying additional rotation.
     */
    default PointGenerator rotate(float pitch, float yaw) {
        return pitch == 0 && yaw == 0 ? this : new RotatedPointGenerator(this, pitch, yaw);
    }

    default PointGenerator translate(Vec3i offset) {
        return offset.equals(Vec3i.ZERO) ? this : translate(Vec3.atLowerCornerOf(offset));
    }

    default PointGenerator translate(Vec3 offset) {
        return offset.equals(Vec3.ZERO) ? this : new TranslatedPointGenerator(this, offset);
    }
}
