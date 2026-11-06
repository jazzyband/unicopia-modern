package com.minelittlepony.unicopia.entity.ai;

import java.util.Comparator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class PrioritizedActiveTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

    private final Comparator<T> prioritySorting;

    public PrioritizedActiveTargetGoal(Mob mob, Class<T> targetClass, Comparator<T> prioritySorting, boolean checkVisibility) {
        super(mob, targetClass, checkVisibility);
        this.prioritySorting = prioritySorting;
    }

    public PrioritizedActiveTargetGoal(Mob mob, Class<T> targetClass, Comparator<T> prioritySorting, boolean checkVisibility, boolean checkCanNavigate) {
        super(mob, targetClass, 10, checkVisibility, checkCanNavigate, null);
        this.prioritySorting = prioritySorting;
    }

    @Override
    protected void findTarget() {
        target = TargettingUtil.getTargets(targetType, targetConditions, mob, getTargetSearchArea(getFollowDistance()))
                .sorted(prioritySorting.thenComparing(TargettingUtil.nearestTo(mob)))
                .findFirst()
                .orElse(null);
    }
}
