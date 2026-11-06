package com.minelittlepony.unicopia.entity.collision;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.google.common.base.Suppliers;
import com.google.common.collect.Streams;

public final class MultiBox extends AABB {
    private final AABB first;
    private final BoxChildren children;

    public static MultiBox of(AABB first, List<AABB> children, List<AABB> nonCollidingChildren) {
        return new MultiBox(first, new BoxChildren(children, nonCollidingChildren));
    }

    public static AABB unbox(AABB box) {
        return box instanceof MultiBox m ? m.first : box;
    }

    public static void forEach(AABB box, Consumer<AABB> consumer) {
        if (box instanceof MultiBox m) {
            m.children.forEach(consumer);
        }
    }

    private MultiBox(AABB first, BoxChildren children) {
        super(first.minX, first.minY, first.minZ, first.maxX, first.maxY, first.maxZ);
        this.first = unbox(first);
        this.children = children;
    }

    @Override
    public Optional<Vec3> clip(Vec3 min, Vec3 max) {
        return super.clip(min, max).or(() -> children.raycast(min, max));
    }

    @Override
    public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return super.intersects(minX, minY, minZ, maxX, maxY, maxZ) || children.intersects(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return super.contains(x, y, z) || children.contains(x, y, z);
    }

    @Override
    public AABB setMinX(double minX) {
        return new MultiBox(super.setMinX(minX), children);
    }

    @Override
    public AABB setMinY(double minY) {
        return new MultiBox(super.setMinY(minY), children);
    }

    @Override
    public AABB setMinZ(double minZ) {
        return new MultiBox(super.setMinZ(minZ), children);
    }

    @Override
    public AABB setMaxX(double maxX) {
        return new MultiBox(super.setMaxX(maxX), children);
    }

    @Override
    public AABB setMaxY(double maxY) {
        return new MultiBox(super.setMaxY(maxY), children);
    }

    @Override
    public AABB setMaxZ(double maxZ) {
        return new MultiBox(super.setMaxZ(maxZ), children);
    }

    @Override
    public AABB contract(double x, double y, double z) {
        return new MultiBox(super.contract(x, y, z), children.altered(b -> b.contract(x, y, z)));
    }

    @Override
    public AABB expandTowards(double x, double y, double z) {
        return new MultiBox(super.expandTowards(x, y, z), children.altered(b -> b.expandTowards(x, y, z)));
    }

    @Override
    public AABB inflate(double x, double y, double z) {
        return new MultiBox(super.inflate(x, y, z), children.altered(b -> b.inflate(x, y, z)));
    }

    @Override
    public AABB intersect(AABB box) {
        return new MultiBox(super.intersect(box), children);
    }

    @Override
    public AABB minmax(AABB box) {
        return new MultiBox(super.minmax(box), children);
    }

    @Override
    public AABB move(double x, double y, double z) {
        return new MultiBox(super.move(x, y, z), children.altered(b -> b.move(x, y, z)));
    }

    @Override
    public AABB move(BlockPos blockPos) {
        return new MultiBox(super.move(blockPos), children.altered(b -> b.move(blockPos)));
    }

    @Override
    public String toString() {
        return "MULTI_AABB[" + minX + ", " + minY + ", " + minZ + "] -> [" + maxX + ", " + maxY + ", " + maxZ + "]{" + children + "}";
    }

    static final class BoxChildren {
        private final AABB[] children;
        private final AABB[] nonCollidingChildren;
        private final Supplier<String> toString;

        private BoxChildren(AABB[] children, AABB[] nonCollidingChildren) {
            this.children = children;
            this.nonCollidingChildren = nonCollidingChildren;
            toString = Suppliers.memoize(() -> Streams.concat(Arrays.stream(this.nonCollidingChildren), Arrays.stream(this.children)).map(AABB::toString).collect(Collectors.joining(",")));
        }

        public BoxChildren(List<AABB> children, List<AABB> nonCollidingChildren) {
            this(children.stream().map(MultiBox::unbox).toArray(AABB[]::new), nonCollidingChildren.stream().map(MultiBox::unbox).toArray(AABB[]::new));
        }

        public BoxChildren altered(Function<AABB, AABB> alteration) {
            BoxChildren copy = new BoxChildren(new AABB[children.length], new AABB[nonCollidingChildren.length]);
            for (int i = 0; i < children.length; i++) {
                copy.children[i] = alteration.apply(children[i]);
            }
            for (int i = 0; i < nonCollidingChildren.length; i++) {
                copy.nonCollidingChildren[i] = alteration.apply(nonCollidingChildren[i]);
            }
            return copy;
        }

        public Optional<Vec3> raycast(Vec3 min, Vec3 max) {
            Optional<Vec3> trace = Optional.empty();
            for (int i = 0; trace.isEmpty() && i < children.length; i++) {
                trace = children[i].clip(min, max);
            }
            for (int i = 0; trace.isEmpty() && i < nonCollidingChildren.length; i++) {
                trace = nonCollidingChildren[i].clip(min, max);
            }
            return trace;
        }

        public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
                    return true;
                }
            }
            return false;
        }

        public boolean contains(double x, double y, double z) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].contains(x, y, z)) {
                    return true;
                }
            }
            return false;
        }

        public void forEach(Consumer<AABB> consumer) {
            for (int i = 0; i < children.length; i++) {
                consumer.accept(children[i]);
            }
            for (int i = 0; i < nonCollidingChildren.length; i++) {
                consumer.accept(nonCollidingChildren[i]);
            }
        }

        @Override
        public String toString() {
            return toString.get();
        }
    }
}
