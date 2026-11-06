package com.minelittlepony.unicopia.entity.ai;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Living;

public class FleeExplosionGoal extends Goal {
    private static final Predicate<Entity> SOURCE_PREDICATE = e -> e instanceof MinecartTNT || e instanceof PrimedTnt;

    private final PathfinderMob mob;
    private final double slowSpeed;
    private final double fastSpeed;
    private final Comparator<Entity> sorting;

    @Nullable
    private Entity targetEntity;
    @Nullable
    private Path fleePath;

    public FleeExplosionGoal(PathfinderMob mob, float distance, double slowSpeed, double fastSpeed) {
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.mob = mob;
        this.slowSpeed = slowSpeed;
        this.fastSpeed = fastSpeed;
        this.sorting = Comparator.comparingDouble(e -> e.distanceToSqr(mob));
    }

    public void setFleeTarget(@Nullable Entity target) {
        this.targetEntity = target;
    }

    @Override
    public boolean canUse() {
        if (targetEntity == null || targetEntity.isRemoved()) {
            targetEntity = mob.level().getEntities(mob, mob.getBoundingBox().inflate(5, 3, 5), SOURCE_PREDICATE).stream().sorted(sorting).findFirst().orElse(null);
        }

        if (targetEntity == null) {
            return false;
        }

        Vec3 targetPosition = DefaultRandomPos.getPosAway(mob, 16, 7, targetEntity.position());
        if (targetPosition == null
                || targetEntity.distanceToSqr(targetPosition.x, targetPosition.y, targetPosition.z) < targetEntity.distanceToSqr(mob)) {
            return false;
        }
        fleePath = mob.getNavigation().createPath(targetPosition.x, targetPosition.y, targetPosition.z, 0);
        return fleePath != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(fleePath, slowSpeed);
    }

    @Override
    public void stop() {
        targetEntity = null;
    }

    @Override
    public void tick() {
        if (mob.distanceToSqr(targetEntity) < 49.0) {
            mob.getNavigation().setSpeedModifier(fastSpeed);
        } else {
            mob.getNavigation().setSpeedModifier(slowSpeed);
        }
    }

    public static void notifySurroundings(Entity explosionSource, float radius) {
        explosionSource.level().getEntities(explosionSource, explosionSource.getBoundingBox().inflate(radius), e -> {
           return Living.getOrEmpty(e).filter(l -> l instanceof Creature).isPresent();
        }).forEach(e -> {
            getGoals((Creature)Living.living(e)).forEach(goal -> goal.setFleeTarget(explosionSource));
        });
    }

    private static Stream<FleeExplosionGoal> getGoals(Creature creature) {
        return creature.getGoals().stream()
                .flatMap(goals -> goals.getAvailableGoals().stream())
                .map(WrappedGoal::getGoal)
                .filter(g -> g instanceof FleeExplosionGoal)
                .map(FleeExplosionGoal.class::cast);
    }
}