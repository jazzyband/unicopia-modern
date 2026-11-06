package com.minelittlepony.unicopia.util.shape;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.include.com.google.common.base.Objects;

/**
 * A sphere, or 2d circle of you so desire.
 *
 */
public class Sphere implements Shape {
    public static final Sphere UNIT_SPHERE = new Sphere(false, 1);

    protected final Vec3 stretch;
    private final boolean hollow;
    private final double rad;

    private final double volume;

    /**
     * Creates a uniform sphere.
     *
     * @param hollow    True if this shape must be hollow.
     * @param radius    Sphere radius
     */
    public Sphere(boolean hollow, double radius) {
        this(hollow, radius, 1, 1, 1);
    }

    /**
     * Creates a sphere of arbitrary dimensions.
     * <p>
     * Can be used to create a flat circle by setting one of the stretch parameters to 0.
     * If you set two of them to 0 it will probably produce a line.
     *
     * @param hollow    True if this shape must be hollow.
     * @param radius    Sphere radius
     * @param stretchX    Warp this shape's X-axis
     * @param stretchY    Warp this shape's Y-axis
     * @param stretchZ    Warp this shape's Z-axis
     *
     */
    public Sphere(boolean hollow, double radius, float stretchX, float stretchY, float stretchZ) {
        this.hollow = hollow;
        stretch = new Vec3(stretchX, stretchY, stretchZ);
        rad = radius;
        volume = computeSpawnableSpace();
    }

    @Override
    public double getVolume() {
        return volume;
    }

    private double computeSpawnableSpace() {
        if (!hollow) {
            return computeEllipsoidVolume(rad, stretch);
        }

        if (stretch.x == stretch.y && stretch.y == stretch.z) {
            double radius = rad * stretch.x;
            return 4 * Math.PI * radius * radius;
        }
        return computeEllipsoidArea(rad, stretch);
    }

    @Override
    public Vec3 computePoint(RandomSource rand) {
        double rad = this.rad;

        if (!hollow) {
            rad = Mth.nextDouble(rand, 0, rad);
        }

        double z = Mth.nextDouble(rand, -rad, rad);
        double phi = Mth.nextDouble(rand, 0, Math.PI * 2);
        double theta = Math.asin(z / rad);

        return new Vec3(
                rad * Math.cos(theta) * Math.cos(phi) * stretch.x,
                rad * Math.cos(theta) * Math.sin(phi) * stretch.y,
                z * stretch.z
        );
    }

    @Override
    public boolean isPointInside(Vec3 point) {
        point = new Vec3(point.x / stretch.x, point.y / stretch.y, point.z / stretch.z);

        double dist = point.length();

        return hollow ? dist == rad : dist <= rad;
    }

    @Override
    public Vec3 getLowerBound() {
        return stretch.scale(-rad);
    }

    @Override
    public Vec3 getUpperBound() {
        return stretch.scale(rad);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Sphere o
                && Objects.equal(stretch, o.stretch)
                && hollow == o.hollow
                && Double.compare(rad, o.rad) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stretch, hollow, rad);
    }

    public static double computeEllipsoidArea(double rad, Vec3 stretch) {
        double p = 1.6075;
        double result = Math.pow(rad * stretch.x, p) * Math.pow(rad * stretch.y, p);
        result += Math.pow(rad * stretch.x, p) * Math.pow(rad * stretch.z, p);
        result += Math.pow(rad * stretch.y, p) * Math.pow(rad * stretch.y, p);
        result /= 3;
        return 2 * Math.PI * Math.pow(result, 1/p);
    }

    public static double computeEllipsoidVolume(double rad, Vec3 stretch) {
        double result = (4/3) * Math.PI;
        result *= (rad * stretch.x);
        result *= (rad * stretch.y);
        result *= (rad * stretch.z);
        return result;
    }
}
