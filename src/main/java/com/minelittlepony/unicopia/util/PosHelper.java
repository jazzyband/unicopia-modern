package com.minelittlepony.unicopia.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public interface PosHelper {
    Direction[] ALL = Direction.values();
    Direction[] HORIZONTAL = Arrays.stream(Direction.values()).filter(d -> d.getAxis().isHorizontal()).toArray(Direction[]::new);

    static Stream<BlockPos> withinManhattanStream(BlockPos center, int xRadius, int yRadius, int zRadius) {
        return BlockPos.betweenClosedStream(center.offset(-xRadius, -yRadius, -zRadius),
                center.offset(xRadius, yRadius, zRadius))
                .sorted(Comparator.comparingInt(pos -> Math.abs(pos.getX() - center.getX())
                        + Math.abs(pos.getY() - center.getY()) + Math.abs(pos.getZ() - center.getZ())));
    }

    static Vec3 offset(Vec3 a, Vec3i b) {
        return a.add(b.getX(), b.getY(), b.getZ());
    }

    static BlockPos findNearestSurface(Level world, BlockPos pos) {
        BlockPos topPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
        if (topPos.getY() < pos.getY()) {
            return topPos;
        }
        BlockPos.MutableBlockPos mutable = pos.mutable();

        while (mutable.getY() > world.getMinY() && world.isEmptyBlock(mutable)) {
            mutable.move(Direction.DOWN);
        }
        while (world.isInWorldBounds(mutable) && !world.isEmptyBlock(mutable)) {
            mutable.move(Direction.UP);
        }
        mutable.move(Direction.DOWN);

        return mutable;
    }

    static BlockPos findSolidGroundAt(Level world, BlockPos pos, int signum) {
        BlockPos topPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
        if (topPos.getY() < pos.getY()) {
            return topPos;
        }
        BlockPos.MutableBlockPos mutable = pos.mutable();
        while (world.isInWorldBounds(mutable) && (world.isEmptyBlock(mutable) || !world.getBlockState(mutable).canSurvive(world, mutable))) {
            mutable.move(Direction.DOWN, signum);
        }

        return mutable.immutable();
    }

    static boolean isOverVoid(Level world, BlockPos pos, int signum) {
        return signum > 0 && findSolidGroundAt(world, pos, signum).getY() < world.getMinY();
    }

    static void fastAll(BlockPos origin, Consumer<BlockPos.MutableBlockPos> consumer, Direction... directions) {
        final BlockPos immutable = origin instanceof BlockPos.MutableBlockPos m ? m.immutable() : origin;
        final BlockPos.MutableBlockPos mutable = origin instanceof BlockPos.MutableBlockPos m ? m : origin.mutable();
        for (Direction facing : directions) {
            consumer.accept(mutable.move(facing));
            mutable.set(immutable);
        }
    }

    static boolean fastAny(BlockPos origin, Predicate<BlockPos> consumer, Direction... directions) {
        return fastAny(origin, (p, f) -> consumer.test(p), directions);
    }

    static boolean fastAny(BlockPos origin, BiPredicate<BlockPos, Direction> consumer, Direction... directions) {
        final BlockPos immutable = origin instanceof BlockPos.MutableBlockPos m ? m.immutable() : origin;
        final BlockPos.MutableBlockPos mutable = origin instanceof BlockPos.MutableBlockPos m ? m : origin.mutable();
        try {
            for (Direction facing : directions) {
                if (consumer.test(mutable.set(immutable).move(facing), facing)) {
                    return true;
                }
            }
            return false;
        } finally {
            mutable.set(immutable);
        }
    }

    static Stream<BlockPos> adjacentNeighbours(BlockPos origin) {
        return StreamSupport.stream(new AbstractSpliterator<BlockPos>(Direction.values().length, Spliterator.SIZED) {
            private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            private final Iterator<Direction> iter = Lists.newArrayList(Direction.values()).iterator();

            @Override
            public boolean tryAdvance(Consumer<? super BlockPos> consumer) {
                if (iter.hasNext()) {
                    Direction next = iter.next();
                    consumer.accept(pos.set(origin.getX() + next.getStepX(), origin.getY() + next.getStepY(), origin.getZ() + next.getStepZ()));
                    return true;
                }
                return false;
            }
        }, false);
    }

    static BlockPos traverseChain(BlockGetter world, BlockPos startingPos, Direction chainDirection, Predicate<BlockState> isInChain) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        mutablePos.set(startingPos);
        do {
            mutablePos.move(chainDirection);
        } while (isInChain.test(world.getBlockState(mutablePos)) && !world.isOutsideBuildHeight(mutablePos));

        mutablePos.move(chainDirection.getOpposite());
        return mutablePos.immutable();
    }

    public final class PositionRecord {
        private final LongOpenHashSet visitedPositions = new LongOpenHashSet();

        public BlockPos visit(BlockPos pos) {
            visitedPositions.add(pos.asLong());
            return pos;
        }

        public boolean hasVisited(BlockPos pos) {
            return visitedPositions.contains(pos.asLong());
        }

        public int size() {
            return visitedPositions.size();
        }

        public void clear() {
            visitedPositions.clear();
        }

        public void forEach(Level world, Reactor reactor) {
            forEach(new BlockPos.MutableBlockPos(), pos -> reactor.react(world, world.getBlockState(pos), pos));
        }

        public void forEach(BlockPos.MutableBlockPos mutable, Consumer<BlockPos> consumer) {
            visitedPositions.forEach(l -> consumer.accept(mutable.set(l)));
        }

        public interface Reactor {
            void react(Level w, BlockState state, BlockPos pos);
        }
    }
}
