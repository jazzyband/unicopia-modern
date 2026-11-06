package com.minelittlepony.unicopia.entity.ai;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.VecHelper;

public class DynamicTargetGoal extends Goal {

    private final Mob mob;

    private int interval;

    private Optional<Entity> target = Optional.empty();

    @Nullable
    private Predicate<Entity> test;

    public DynamicTargetGoal(Mob mob) {
        this.mob = mob;
    }

    public Supplier<Optional<Entity>> addPredicate(Predicate<Entity> predicate) {
        test = test == null ? predicate : predicate.or(test);

        return () -> target.filter(Entity::isAlive).filter(predicate);
    }

    public Optional<Entity> getTarget() {
        if (test == null) {
            return target.filter(Entity::isAlive);
        }
        return target.filter(test).filter(Entity::isAlive);
    }

    @Override
    public boolean canUse() {
        if (interval-- <= 0) {
            interval = 20;

            if (mob.getTarget() != null && mob.getTarget().isDeadOrDying()) {
                mob.setTarget(null);
            }

            target = VecHelper.findInRange(mob, mob.level(), mob.position(), 26, test)
                .stream()
                .sorted(Comparator.comparing(e -> mob.distanceTo(e)))
                .findFirst();

            if (target.isPresent()) {
                if (target.get() instanceof LivingEntity) {
                    mob.setTarget((LivingEntity)target.get());
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void stop() {
        target = Optional.empty();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return target != null && TargetingConditions.DEFAULT.test(getServerLevel(mob.level()), mob, target);
    }
}
