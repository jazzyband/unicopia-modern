package com.minelittlepony.unicopia.util.shape;

import com.minelittlepony.unicopia.util.VecHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * A lonely Line. The simplest form of shape.
 */
public class Line implements Shape {

    private final double length;

    private final Vec3 gradient;

    public Line(Vec3 start, Vec3 end) {
        Vec3 lenV = end.subtract(start);
        length = lenV.length();
        this.gradient = lenV.scale(1/length);
    }

    public Line(double length, Vec3 gradient) {
        this.length = length;
        this.gradient = gradient.normalize();
    }

    @Override
    public double getVolume() {
        return length;
    }

    @Override
    public Vec3 computePoint(RandomSource rand) {
        return gradient.scale(Mth.nextDouble(rand, 0, length));
    }

    @Override
    public boolean isPointInside(Vec3 point) {
        Vec3 divided = VecHelper.divide(point, gradient);
        return divided.x == divided.y && divided.x == divided.z;
    }

    @Override
    public Vec3 getLowerBound() {
        return Vec3.ZERO;
    }

    @Override
    public Vec3 getUpperBound() {
        return gradient.scale(length);
    }
}
