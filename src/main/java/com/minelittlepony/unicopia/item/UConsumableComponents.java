package com.minelittlepony.unicopia.item;

import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public interface UConsumableComponents {
    Consumable OATMEAL = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.HEALTH_BOOST, 1200, 1)), 1))
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.STRENGTH, 1200, 1)), 0.3F))
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.SPEED, 1200, 1)), 0.2F)).build();

    Consumable ZAP_BULB = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                    new MobEffectInstance(MobEffects.POISON, 100, 0),
                    new MobEffectInstance(MobEffects.BLINDNESS, 100, 0),
                    new MobEffectInstance(MobEffects.BAD_OMEN, 100, 0)
            ), 0.6F))
            .soundAfterConsume(SoundEvents.OMINOUS_BOTTLE_DISPOSE)
            .build();

    Consumable SNACK = Consumables.defaultFood().consumeSeconds(0.8F).build();

    static Consumable poisonedFish(float poisonChance, float hungerChance, float nauseaChance) {
        return Consumables.defaultFood()
                .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.POISON, 1200, 1)), poisonChance))
                .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.HUNGER, 300, 2)), hungerChance))
                .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.NAUSEA, 300, 0)), nauseaChance))
                .build();
    }
}
