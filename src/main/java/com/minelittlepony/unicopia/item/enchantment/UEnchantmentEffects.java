package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.Unicopia;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;

public interface UEnchantmentEffects {
    static void bootstrap() {
        register("group_based_attribute", GroupBasedAttributeEnchantmentEffect.CODEC);
        register("ambient_sounds", AmbientSoundsEnchantmentEffect.CODEC);
        register("danger_sensor", DangerSensingEnchantmentEffect.CODEC);
        register("particle_trail", ParticleTrailEnchantmentEntityEffect.CODEC);
    }

    private static void register(String name, MapCodec<? extends EnchantmentEntityEffect> codec) {
        Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Unicopia.id(name), codec);
    }
}
