package com.minelittlepony.unicopia.util.shape;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

record RotatedPointGenerator (
        PointGenerator original,
        float pitch,
        float yaw
    ) implements Shape {
    @Override
    public double getVolume() {
        return ((Shape)original).getVolume();
    }

    @Override
    public Vec3 computePoint(RandomSource rand) {
        return rotate(original.computePoint(rand));
    }

    @Override
    public Vec3 getLowerBound() {
        return rotate(((Shape)original).getLowerBound());
    }

    @Override
    public Vec3 getUpperBound() {
        return rotate(((Shape)original).getUpperBound());
    }

    @Override
    public boolean isPointInside(Vec3 point) {
        return ((Shape)original).isPointInside(point.yRot(-yaw).xRot(-pitch));
    }

    private Vec3 rotate(Vec3 vec) {
        return vec.xRot(pitch).yRot(yaw);
    }

    @Override
    public Shape rotate(float pitch, float yaw) {
        if (pitch == 0 && yaw == 0) {
            return this;
        }

        return new RotatedPointGenerator(this, this.pitch + pitch, this.yaw + yaw);
    }
}
