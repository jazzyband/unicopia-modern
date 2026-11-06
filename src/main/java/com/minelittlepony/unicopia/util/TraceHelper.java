package com.minelittlepony.unicopia.util;

import java.util.*;
import java.util.function.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class TraceHelper {
    /**
     * Performs a ray trace from the given entity and returns
     * a result for the first Entity that the ray intercepts.
     *
     *
     * @param e            Entity to start from
     * @param distance     Maximum distance
     * @param tickDelta    Client partial ticks
     */
    public static <T extends Entity> Optional<T> findEntity(Entity e, double distance, float tickDelta, Predicate<Entity> predicate) {
        return traceEntity(e, distance, tickDelta, EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(predicate)).map(Trace::toEntity);
    }

    /**
     * Performs a ray trace from the given entity and returns
     * a result for the first Entity that the ray intercepts.
     *
     *
     * @param e            Entity to start from
     * @param distance     Maximum distance
     * @param tickDelta    Client partial ticks
     */
    public static <T extends Entity> Optional<T> findEntity(Entity e, double distance, float tickDelta) {
        return traceEntity(e, distance, tickDelta, EntitySelector.NO_CREATIVE_OR_SPECTATOR).map(Trace::toEntity);
    }

    /**
     * Performs a ray trace from the given entity and returns
     * a result for the first block that the ray intercepts.
     *
     *
     * @param e            Entity to start from
     * @param distance     Maximum distance
     * @param tickDelta    Client partial ticks
     */
    public static Optional<BlockPos> findBlock(Entity e, double distance, float tickDelta) {
        return Trace.create(e, distance, tickDelta, EntitySelector.NO_CREATIVE_OR_SPECTATOR).getBlockPos();
    }

    /**
     * Performs a raytrace and returns all matching blocks the ray passed through.
     *
     * @param e            Entity to start from
     * @param distance     Maximum distance
     * @param tickDelta    Client partial ticks
     * @param predicate    Predicate test to filter block states
     *
     * @return List of matching positions
     */
    public static List<BlockPos> findBlocks(Entity e, double distance, float tickDelta, Predicate<BlockState> predicate) {
        final Vec3 orientation = e.getViewVector(tickDelta);
        final Vec3 start = e.getEyePosition(tickDelta);

        return BlockGetter.traverseBlocks(start, start.add(orientation.scale(distance)), new ArrayList<BlockPos>(), (ctx, pos) -> {
            if (predicate.test(e.level().getBlockState(pos))) {
                ctx.add(pos);
            }
            return null;
        }, ctx -> {
            return ctx;
        });
    }

    static Optional<EntityHitResult> traceEntity(Entity e, double distance, float tickDelta, Predicate<Entity> predicate) {
        final Vec3 orientation = e.getViewVector(tickDelta);
        final Vec3 start = e.getEyePosition(tickDelta);

        final AABB box = e.getBoundingBox().expandTowards(orientation.scale(Math.abs(distance))).inflate(10);

        return Optional.ofNullable(
            ProjectileUtil.getEntityHitResult(e, start, start.add(orientation.scale(distance)), box, predicate, Math.abs(distance))
        );
    }
}
