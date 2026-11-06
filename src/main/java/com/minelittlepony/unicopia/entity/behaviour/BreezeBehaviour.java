package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge;
import net.minecraft.world.phys.Vec3;

public class BreezeBehaviour extends EntityBehaviour<Breeze> {
    @Override
    public void update(Living<?> source, Breeze entity, Disguise spell) {
        super.update(source, entity, spell);

        Entity src = source.asEntity();

        if (src.onGround() || src instanceof Player player && player.getAbilities().flying) {
            return;
        }

        Vec3 vel = src.getDeltaMovement();

        if (vel.y < 0) {
            src.setDeltaMovement(vel.multiply(1, 0.8, 1));
        }

        if (!source.asEntity().onGround()) {
            vel = vel.multiply(1.2, 1, 1.2);

            src.setDeltaMovement(Mth.clamp(vel.x, -0.5, 0.5), vel.y, Mth.clamp(vel.z, -0.5, 0.5));
        }
    }

    @Override
    public void update(Pony player, Breeze entity, Disguise spell) {

        if (!player.isClient()) {

            if (!player.asEntity().hasEffect(MobEffects.JUMP_BOOST)) {
                player.asEntity().addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 10, 2, false, false));
            }
        }

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
                entity.setPose(Pose.SHOOTING);
            } else if (fireballsFired <= 4) {
                fireballCooldown = 6;
            } else {
                fireballCooldown = 100;
                fireballsFired = 0;
                entity.setPose(player.asEntity().onGround() ? Pose.STANDING : Pose.LONG_JUMPING);
            }

            if (fireballsFired > 0) {
                entity.playSound(SoundEvents.BREEZE_SHOOT, 1.5F, 1.0F);

                Vec3 rot = player.asEntity().getViewVector(1);

                for (int i = 0; i < 1; ++i) {
                    BreezeWindCharge proj = new BreezeWindCharge(entity, entity.level());
                    proj.setDeltaMovement(rot.add(entity.getRandom().nextGaussian() * 0.1, 0, entity.getRandom().nextGaussian() * 0.1));
                    proj.setPos(proj.getX(), entity.getY(0.5D) + 0.5D, proj.getZ());
                    entity.level().addFreshEntity(proj);
                }
            }
        } else if (!firing) {
            entity.setPose(player.asEntity().onGround() ? Pose.STANDING : Pose.LONG_JUMPING);
        }

        tag.putBoolean("isFiring", firing);
        tag.putInt("fireballCooldown", fireballCooldown);
        tag.putInt("fireballsFired", fireballsFired);
    }
}
