package com.minelittlepony.unicopia.entity.effect;

import com.minelittlepony.unicopia.InteractionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface EffectUtils {
    static boolean isPoisoned(LivingEntity entity) {
        return getAmplifier(entity, UEffects.FOOD_POISONING) > 1;
    }

    static boolean hasABrokenWing(LivingEntity entity) {
        return entity.hasEffect(UEffects.BROKEN_WINGS);
    }

    static boolean hasBothBrokenWing(LivingEntity entity) {
        return getAmplifier(entity, UEffects.BROKEN_WINGS) > 1;
    }

    static int getAmplifier(LivingEntity entity, Holder<MobEffect> effect) {
        return entity.hasEffect(effect) ? entity.getEffect(effect).getAmplifier() + 1 : 0;
    }

    static boolean isChangingRace(LivingEntity entity) {
        return entity.getActiveEffects().stream().anyMatch(effect -> effect.getEffect().value() instanceof RaceChangeStatusEffect);
    }

    static boolean hasExtraDefenses(LivingEntity entity) {
        return entity.hasEffect(UEffects.FORTIFICATION);
    }

    static boolean applyStatusEffect(LivingEntity entity, Holder<MobEffect> effect, boolean apply) {
        if (entity.level().isClientSide()) {
            return false;
        }
        boolean has = entity.hasEffect(effect);
        if (has != apply) {
            if (has) {
                if (entity.getEffect(effect).getDuration() == MobEffectInstance.INFINITE_DURATION) {
                    entity.removeEffect(effect);
                }
            } else {
                entity.addEffect(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, 0, false, false, true));
            }
            return true;
        }
        return false;
    }

    static Component formatModifierChange(String modifierName, int time, boolean isDetrimental) {
        return Component.literal(" ").append(Component.translatable("attribute.modifier." + (time > 0 ? "plus" : "take") + ".0",
                StringUtil.formatTickDuration(Math.abs(time), InteractionManager.getInstance().getTickRate()),
                Component.translatable(modifierName)
        ).withStyle((isDetrimental ? time : -time) < 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.RED));
    }

    static Component formatModifierChange(String modifierName, float change, boolean isDetrimental) {
        return Component.literal(" ").append(Component.translatable("attribute.modifier." + (change > 0 ? "plus" : "take") + ".0",
                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(change)),
                Component.translatable(modifierName)
        ).withStyle((isDetrimental ? change : -change) < 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.RED));
    }

    static Component formatModifierChange(Component modifierName, float change, boolean isDetrimental) {
        return Component.literal(" ").append(Component.translatable("attribute.modifier." + (change > 0 ? "plus" : "take") + ".0",
                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(change)),
               modifierName
        ).withStyle((isDetrimental ? change : -change) < 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.RED));
    }
}
