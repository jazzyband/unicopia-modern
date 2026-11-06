package com.minelittlepony.unicopia.util.shape;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

record TranslatedPointGenerator (
        PointGenerator source,
        Vec3 offset
    ) implements Shape {

    @Override
    public double getVolume() {
        return ((Shape)source).getVolume();
    }

    @Override
    public Vec3 computePoint(RandomSource rand) {
        return source.computePoint(rand).add(offset);
    }

    @Override
    public Vec3 getLowerBound() {
        return ((Shape)source).getLowerBound().add(offset);
    }

    @Override
    public Vec3 getUpperBound() {
        return ((Shape)source).getUpperBound().add(offset);
    }

    @Override
    public boolean isPointInside(Vec3 point) {
        return ((Shape)source).isPointInside(point.subtract(offset));
    }

    @Override
    public Shape translate(Vec3 offset) {
        if (offset.equals(Vec3.ZERO)) {
            return this;
        }
        return new TranslatedPointGenerator(source, this.offset.add(offset));
    }
}
