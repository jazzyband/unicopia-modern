package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;

public class GhastBehaviour extends MobBehaviour<Ghast> {

    @Override
    public void update(Pony player, Ghast entity, Disguise spell) {

        if (player.sneakingChanged()) {
            boolean sneaking = player.asEntity().isShiftKeyDown();
            entity.setCharging(sneaking);
            entity.setTarget(sneaking ? findTarget(player, entity) : null);

            if (sneaking) {
                if (!entity.isSilent()) {
                    entity.level().levelEvent(null, LevelEvent.SOUND_GHAST_WARNING, entity.blockPosition(), 0);
                }
            } else {
                if (!entity.isSilent()) {
                    entity.level().levelEvent(null, LevelEvent.SOUND_GHAST_FIREBALL, entity.blockPosition(), 0);
                }

                Vec3 rot = player.asEntity().getViewVector(1);

                LargeFireball proj = new LargeFireball(entity.level(), player.asEntity(), rot,
                        (int)player.getLevel().getScaled(entity.getExplosionPower())
                );
                proj.absSnapTo(
                        entity.getX() + rot.x * 4,
                        entity.getY(0.5D) + 0.5,
                        proj.getZ() + rot.z * 4
                );

                entity.level().addFreshEntity(proj);
            }
        }
    }

    @Override
    protected Ghast getDummy(Ghast entity) {
        Ghast dummy = super.getDummy(entity);

        Vec3 pos = entity.getEyePosition(1).add(entity.getViewVector(1));
        dummy.setPosRaw(pos.x, pos.y, pos.z);
        return dummy;
    }
}
