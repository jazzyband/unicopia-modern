package com.minelittlepony.unicopia.ability.magic.spell.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.effect.EffectUtils;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public interface AttractionUtils {

    static double getAttractiveForce(double wellMass, Vec3 wellPosition, Entity target) {
        return (wellMass * AttractionUtils.getMass(target)) / Mth.square(wellPosition.distanceTo(target.position()));
    }

    static double getMass(Entity entity) {
        double baseMass = entity.getBbWidth() * entity.getBbHeight();
        if (entity instanceof ItemEntity item) {
            baseMass *= item.getItem().getCount();
            @Nullable
            Block block = Block.byItem(item.getItem().getItem());
            if (block != null) {
                baseMass *= Mth.clamp(1 + block.defaultDestroyTime(), 1, 5);
            }
        }
        return baseMass;
    }

    /**
     * Applies a force to the given entity relative to a given center position
     */
    static void applyForce(Vec3 center, Entity target, double force, double yChange, boolean clampVelocity) {
        if (clampVelocity) {
            target.setDeltaMovement(target.getDeltaMovement().scale(Math.min(1, 1 - force)));
        }
        center = target.position().subtract(center).normalize().scale(force);

        if (target instanceof LivingEntity) {
            center = center.scale(1 / EnchantmentUtil.getWeight((LivingEntity)target));
        }

        target.push(
                center.x,
                center.y + yChange,
                center.z
        );
        Living.updateVelocity(target);
    }

    /**
     * Returns a force to apply based on the given player's given race.
     */
    static double getForceAdjustment(Entity entity) {
        return Pony.of(entity).map(pony -> {
            double force = 0.75;

            if (EffectUtils.hasExtraDefenses(pony.asEntity())) {
                force /= 12;
            } else if (pony.getCompositeRace().canUseEarth()) {
                force /= 2;
            } else if (pony.getCompositeRace().canFly()) {
                force *= 2;
            }

            return force;
        }).orElse(1D);
    }
}
