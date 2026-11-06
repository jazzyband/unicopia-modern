package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.MixinBlazeEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;

public class BlazeBehaviour extends EntityBehaviour<Blaze> {
    @Override
    public void update(Living<?> source, Blaze entity, Disguise spell) {
        super.update(source, entity, spell);

        Entity src = source.asEntity();

        if (src.onGround() || src instanceof Player player && player.getAbilities().flying) {
            return;
        }

        Vec3 vel = src.getDeltaMovement();

        if (vel.y < 0) {
            src.setDeltaMovement(vel.multiply(1, 0.8, 1));
        }
    }

    @Override
    public void update(Pony player, Blaze entity, Disguise spell) {

        CompoundTag tag = spell.getAppearance().getOrCreateTag();

        boolean firing = tag.getBooleanOr("isFiring", false);
        int fireballCooldown = tag.getIntOr("fireballCooldown", 0);
        int fireballsFired = tag.getIntOr("fireballsFired", 0);

        if (player.sneakingChanged()) {
            boolean sneaking = player.asEntity().isShiftKeyDown();

            if (sneaking) {
                firing = true;
                fireballCooldown = 0;
                fireballsFired = 0;
            } else {
                firing = false;
            }
        }

        if (firing && fireballCooldown <= 0) {
            fireballsFired++;

            if (fireballsFired == 1) {
                fireballCooldown = 60;
                ((MixinBlazeEntity)entity).invokeSetCharged(true);
            } else if (fireballsFired <= 4) {
                fireballCooldown = 6;
            } else {
                fireballCooldown = 100;
                fireballsFired = 0;
                ((MixinBlazeEntity)entity).invokeSetCharged(false);
            }

            if (fireballsFired > 0) {
                if (!entity.isSilent()) {
                    entity.level().levelEvent(null, LevelEvent.SOUND_BLAZE_FIREBALL, entity.blockPosition(), 0);
                }

                Vec3 rot = player.asEntity().getViewVector(1);

                for (int i = 0; i < 1; ++i) {
                   SmallFireball proj = new SmallFireball(
                           entity.level(),
                           player.asEntity(),
                           rot.add(entity.getRandom().nextGaussian(), 0, entity.getRandom().nextGaussian())
                   );
                   proj.setPos(proj.getX(), entity.getY(0.5D) + 0.5D, proj.getZ());
                   entity.level().addFreshEntity(proj);
                }
            }
        } else if (!firing) {
            ((MixinBlazeEntity)entity).invokeSetCharged(false);
        }

        tag.putBoolean("isFiring", firing);
        tag.putInt("fireballCooldown", fireballCooldown);
        tag.putInt("fireballsFired", fireballsFired);
    }
}
