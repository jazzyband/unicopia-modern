package com.minelittlepony.unicopia.util;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface VecHelper {

    static Vec3 lerp(float delta, Vec3 a, Vec3 b) {
        return new Vec3(
                Mth.lerp(delta, a.x, b.x),
                Mth.lerp(delta, a.y, b.y),
                Mth.lerp(delta, a.z, b.z)
        );
    }

    static Vec3 divide(Vec3 one, Vec3 two) {
        return new Vec3(one.x / two.x, one.y / two.y, one.z / two.z);
    }

    static Vec3 gaussian(RandomSource random) {
        return supply(() -> random.nextGaussian());
    }

    static Vec3 triangular(RandomSource random, double mode, double deviation) {
        return supply(() -> random.triangle(mode, deviation));
    }

    static Vec3 supply(DoubleSupplier rng) {
        return new Vec3(rng.getAsDouble(), rng.getAsDouble(), rng.getAsDouble());
    }

    static Supplier<Vec3> supplier(DoubleSupplier rng) {
        return () -> supply(rng);
    }

    static Supplier<Vec3> sphere(RandomSource rng) {
        return sphere(rng, 1);
    }

    static Supplier<Vec3> sphere(RandomSource rng, double radius) {
        return supplier(() -> (rng.nextGaussian() - 0.5) * radius);
    }

    static Predicate<Entity> inRange(Vec3 center, double range) {
        double rad = Math.pow(range, 2);
        return e -> {
            return e.distanceToSqr(center) <= rad
                || e.distanceToSqr(center.x(), center.y() - e.getEyeHeight(), center.z()) <= rad;
        };
    }

    static List<Entity> findInRange(@Nullable Entity origin, EntityGetter w, Vec3 pos, double radius, @Nullable Predicate<Entity> predicate) {
        double diameter = radius * 2;
        return w.getEntities(origin, AABB.ofSize(pos, diameter, diameter, diameter), predicate == null ? inRange(pos, radius) : inRange(pos, radius).and(predicate));
    }

    static List<Entity> findInRange(@Nullable Entity origin, EntityGetter w, Vec3 pos, double radius) {
        double diameter = radius * 2;
        return w.getEntities(origin, AABB.ofSize(pos, diameter, diameter, diameter), inRange(pos, radius));
    }
}
