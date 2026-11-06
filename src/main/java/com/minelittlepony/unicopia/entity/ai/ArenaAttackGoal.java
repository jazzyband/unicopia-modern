package com.minelittlepony.unicopia.entity.ai;

import net.minecraft.world.phys.Vec3;

import com.minelittlepony.unicopia.entity.mob.ArenaCombatant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.level.pathfinder.Path;

public class ArenaAttackGoal<E extends Mob & ArenaCombatant> extends OcelotAttackGoal {

    private final E combatant;

    public ArenaAttackGoal(E mob) {
        super(mob);
        this.combatant = mob;
    }

    @Override
    public boolean canUse() {
        return combatant.getHomePos().isPresent() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return combatant.getHomePos().filter(this::isInArena).isPresent() && super.canContinueToUse();
    }

    private boolean isInArena(BlockPos homePos) {
        return combatant.blockPosition().closerThan(homePos, combatant.getAreaRadius());
    }

    @Override
    public void stop() {
        super.stop();
        combatant.getHomePos().ifPresent(home -> {
            if (!isInArena(home)) {
                Path path = combatant.getNavigation().createPath(home, 2, (int)combatant.getAreaRadius() * 2);
                if (path != null) {
                    combatant.getNavigation().moveTo(path, combatant.getSpeed() * 2F);
                } else {
                    var destination = Vec3.atCenterOf(home);
                    combatant.teleportTo(destination);
                    combatant.setPos(destination);
                }
            }
        });
    }
}
