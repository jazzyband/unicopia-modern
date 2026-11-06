package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.util.TypedActionResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

public class FoodPoisoningStatusEffect extends MobEffect {

    FoodPoisoningStatusEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        boolean showParticles = entity.getEffect(entity.registryAccess().lookupOrThrow(Registries.MOB_EFFECT).wrapAsHolder(this)).isVisible();

        if (!entity.hasEffect(MobEffects.NAUSEA) && entity.getRandom().nextInt(12) == 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 1, true, showParticles, false));
        }

        if (entity instanceof Player player) {
            player.getFoodData().addExhaustion(0.5F);
        }

        if (EffectUtils.isPoisoned(entity) && entity.getRandom().nextInt(12) == 0 && !entity.hasEffect(MobEffects.POISON)) {
            MobEffects.POISON.value().applyEffectTick(world, entity, 1);
        }

        return true;
    }

    @Override
    public void applyInstantaneousEffect(ServerLevel world, @Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        applyEffectTick(world, target, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int i = 40 >> amplifier;
        return i <= 0 || duration % i == 0;
    }

    public static TypedActionResult<ItemStack> apply(ItemStack stack, Player user) {
        @Nullable
        FoodProperties food = stack.get(DataComponents.FOOD);

        if (food == null || !user.canEat(food.canAlwaysEat()) || !user.hasEffect(UEffects.FOOD_POISONING)) {
            return TypedActionResult.pass(stack);
        }

        user.level().playSound(null, user.getX(), user.getY(), user.getZ(), USounds.Vanilla.PLAYER_BURP, SoundSource.NEUTRAL,
                1,
                1 + (user.level().getRandom().nextFloat() - user.level().getRandom().nextFloat()) * 0.4f);
        if (!user.level().isClientSide()) {
            user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 1, true, false, false));
        }
        return TypedActionResult.fail(stack);
    }
}
