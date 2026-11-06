package com.minelittlepony.unicopia.entity.effect;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.MeteorlogicalUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class SunBlindnessStatusEffect extends SimpleStatusEffect {
    public static final int MAX_DURATION = 250;

    SunBlindnessStatusEffect(int color) {
        super(MobEffectCategory.NEUTRAL, color, false);
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        MobEffectInstance state = entity.getEffect(getEntry(entity));

        if (state == null || isSunImmune(entity)) {
            return false;
        }

        if (entity.tickCount % 15 == 0) {
            if (!hasSunExposure(entity)) {
                entity.forceAddEffect(new MobEffectInstance(getEntry(entity), (int)(state.getDuration() * 0.8F), Math.max(1, amplifier - 1), true, false), entity);
            } else {
                entity.hurtServer(world, Living.living(entity).damageOf(amplifier == 2 ? UDamageTypes.SUN : UDamageTypes.SUNLIGHT), amplifier / 5F);
            }
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0;
    }

    public static boolean isSunImmune(LivingEntity entity) {
        return entity.hasEffect(MobEffects.BLINDNESS)
                || entity.isOnPortalCooldown()
                || Pony.of(entity).filter(Pony::isSunImmune).isPresent();
    }

    public static boolean hasSunExposure(LivingEntity entity) {

        if (isSunImmune(entity)) {
            return false;
        }

        if (entity.hasEffect(MobEffects.BLINDNESS)) {
            return false;
        }

        if (EquinePredicates.PLAYER_BAT.test(entity) && entity.hasEffect(MobEffects.NIGHT_VISION)) {
            return true;
        }

        if (entity.isUnderWater() || TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.FACE, i -> i.is(UTags.Items.PROTECTS_BAT_PONY_EYES)).findAny().isPresent()) {
            return false;
        }

        return MeteorlogicalUtil.isPositionExposedToSun(entity.level(), entity.blockPosition());
    }
}
