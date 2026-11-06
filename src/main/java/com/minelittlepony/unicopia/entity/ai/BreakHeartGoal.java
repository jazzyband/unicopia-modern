package com.minelittlepony.unicopia.entity.ai;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import com.minelittlepony.unicopia.entity.mob.FloatingArtefactEntity;
import com.minelittlepony.unicopia.item.UItems;

public class BreakHeartGoal extends Goal {

    protected final Mob mob;

    private final DynamicTargetGoal targetter;
    private final Supplier<Optional<Entity>> target;

    public BreakHeartGoal(Mob mob, DynamicTargetGoal targetter) {
        this.mob = mob;
        this.targetter = targetter;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));

        target = targetter.addPredicate(this::canTarget);
    }

    protected boolean canTarget(Entity e) {
        return !e.isRemoved()
                && e instanceof FloatingArtefactEntity artefact
                && artefact.getStack().is(UItems.CRYSTAL_HEART)
                && mob.getSensing().hasLineOfSight(e);
    }

    @Override
    public boolean canUse() {
        return target.get().isPresent();
    }

    @Override
    public boolean canContinueToUse() {
        return target.get().map(mob::distanceToSqr).orElse(0D) <= 225
                && (!mob.getNavigation().isDone() || canUse());
    }

    @Override
    public void stop() {
        targetter.stop();
        mob.getNavigation().stop();
        mob.setTarget(null);
    }

    @Override
    public void tick() {
        target.get().ifPresent(target -> {
            mob.getLookControl().setLookAt(target, 30, 30);

            double reach = mob.getBbWidth() * 2 * mob.getBbWidth() * 2 * target.getBbWidth();
            double distance = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());

            attackTarget(target, reach, distance);
        });
    }

    protected void attackTarget(Entity target, double reach, double distance) {
        double speed = 0.9D;

        if (distance > reach && distance < 16) {
            speed = 1.23;
        } else if (distance < 225) {
            speed = 1.6;
        } else if (distance <= 1) {
            speed = 0.5;
        }

        mob.getNavigation().moveTo(target, speed);

        if (target.getY() >= mob.getY() + 2) {
            reach += 5;
        }

        if (distance <= reach) {
            mob.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT);
            mob.doHurtTarget(getServerLevel(mob.level()), target);
        }
    }
}
