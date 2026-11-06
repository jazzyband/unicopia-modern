package com.minelittlepony.unicopia.entity.ai;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.jetbrains.annotations.Nullable;

public class PredicatedGoal extends Goal {
    private final Goal goal;
    private final BooleanSupplier predicate;

    public static void applyToAll(GoalSelector goals, BooleanSupplier predicate) {
        Set<WrappedGoal> existingTasks = new HashSet<>(goals.getAvailableGoals());
        goals.removeAllGoals(g -> !(g instanceof PredicatedGoal));
        existingTasks.forEach(goal -> {
            if (!(goal.getGoal() instanceof PredicatedGoal)) {
                goals.addGoal(goal.getPriority(), new PredicatedGoal(goal.getGoal(), predicate));
            }
        });
    }

    public PredicatedGoal(Goal goal, BooleanSupplier predicate) {
        this.goal = goal;
        this.predicate = predicate;
    }

    @Override
    public boolean canUse() {
        return predicate.getAsBoolean() && goal.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return predicate.getAsBoolean() && goal.canContinueToUse();
    }

    @Override
    public boolean isInterruptable() {
        return goal.isInterruptable();
    }

    @Override
    public void start() {
        if (predicate.getAsBoolean()) {
            goal.start();
        }
    }

    @Override
    public void stop() {
        goal.stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return goal.requiresUpdateEveryTick();
    }

    @Override
    public void tick() {
        goal.tick();
    }

    @Override
    public void setFlags(EnumSet<Goal.Flag> controls) {
        goal.setFlags(controls);
    }

    @Override
    public EnumSet<Goal.Flag> getFlags() {
        return goal.getFlags();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else {
            return o != null && getClass() == o.getClass() ? goal.equals(((PredicatedGoal)o).goal) : false;
        }
    }

    @Override
    public int hashCode() {
        return goal.hashCode();
    }
}
