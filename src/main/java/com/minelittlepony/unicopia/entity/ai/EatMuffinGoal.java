package com.minelittlepony.unicopia.entity.ai;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.Consumable;

public class EatMuffinGoal extends BreakHeartGoal {

    private int timer;

    private boolean eatingStarted;

    public EatMuffinGoal(Mob mob, DynamicTargetGoal targetter) {
        super(mob, targetter);
    }

    public int getTimer() {
        return eatingStarted ? timer : 0;
    }

    @Override
    public void start() {
        eatingStarted = false;
        timer = adjustedTickDelay(10);
        mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        super.stop();
        eatingStarted = false;
        timer = 0;
    }

    @Override
    public boolean canContinueToUse() {
        return timer > 0 && super.canContinueToUse();
    }

    @Override
    protected boolean canTarget(Entity e) {
        return !e.isRemoved()
                && e instanceof PhysicsBodyProjectileEntity p
                && p.getItem().is(UConventionalTags.Items.MUFFINS)
                && p.getItem().get(DataComponents.FOOD) != null
                && mob.getSensing().hasLineOfSight(e);
    }

    @Override
    protected void attackTarget(Entity target, double reach, double distance) {
        double speed = 1D;

        if (distance > 5) {
            speed += 0.5;
        }
        if (distance < 2) {
            speed += 0.1D;
        }

        mob.getNavigation().moveTo(target, speed);

        if (distance <= reach + 0.5) {
            eatingStarted = true;

            if (target instanceof PhysicsBodyProjectileEntity projectile) {
                @Nullable
                Consumable food = projectile.getItem().get(DataComponents.CONSUMABLE);
                if (food != null) {
                    food.startConsuming(mob, projectile.getItem(), InteractionHand.MAIN_HAND);
                }
                projectile.discard();

                if (mob instanceof Animal animal) {
                    if (mob.level().getRandom().nextInt(12) == 0) {
                        Entity player = ((PhysicsBodyProjectileEntity) target).getOwner();

                        animal.setInLove(player instanceof Player ? (Player)player : null);
                        animal.setInLoveTime(1000);
                    }
                }
            }
        }
    }
}
