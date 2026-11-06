package com.minelittlepony.unicopia.entity.effect;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public interface UEffects {
    Holder<MobEffect> FOOD_POISONING = register("food_poisoning", new FoodPoisoningStatusEffect(3484199));
    Holder<MobEffect> SUN_BLINDNESS = register("sun_blindness", new SunBlindnessStatusEffect(0x886F0F));
    /**
     * Status effect emitted by players with a high level of corruption.
     * When affecting an entity, will give them a random chance to reproduce or duplicate themselves when they die.
     */
    Holder<MobEffect> CORRUPT_INFLUENCE = register("corrupt_influence", new CorruptInfluenceStatusEffect(0x00FF00));
    Holder<MobEffect> PARALYSIS = register("paralysis", new SimpleStatusEffect(MobEffectCategory.HARMFUL, 0, false));
    Holder<MobEffect> FORTIFICATION = register("fortification", new SimpleStatusEffect(MobEffectCategory.BENEFICIAL, 0x000077, false));
    Holder<MobEffect> BROKEN_WINGS = register("broken_wings", new SimpleStatusEffect(MobEffectCategory.BENEFICIAL, 0xEEAA00, false));
    /**
     * Side-effect of wearing the alicorn amulet.
     * Causes the player to lose grip on whatever item they're holding.
     */
    Holder<MobEffect> BUTTER_FINGERS = register("butter_fingers", new ButterfingersStatusEffect(0x888800));

    Holder<MobEffect> SEAPONYS_GRACE = register("seaponys_grace", new SimpleStatusEffect(MobEffectCategory.BENEFICIAL, 0x0000EE, false));
    Holder<MobEffect> SEAPONYS_IRE = register("seaponys_ire", new SimpleStatusEffect(MobEffectCategory.HARMFUL, 0xEE00EE, false));

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Unicopia.id(name), effect);
    }

    static void bootstrap() {}
}
