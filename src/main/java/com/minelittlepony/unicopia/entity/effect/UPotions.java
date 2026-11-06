package com.minelittlepony.unicopia.entity.effect;

import java.util.Objects;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

public interface UPotions {
    MorphingPotion MORPH_EARTH_PONY = new MorphingPotion(Race.EARTH);
    MorphingPotion MORPH_UNICORN = new MorphingPotion(Race.UNICORN);
    MorphingPotion MORPH_PEGASUS = new MorphingPotion(Race.PEGASUS);
    MorphingPotion MORPH_BAT = new MorphingPotion(Race.BAT);
    MorphingPotion MORPH_CHANGELING = new MorphingPotion(Race.CHANGELING);
    MorphingPotion MORPH_KIRIN = new MorphingPotion(Race.KIRIN);
    MorphingPotion MORPH_HIPPOGRIFF = new MorphingPotion(Race.HIPPOGRIFF);

    static Holder<Potion> register(String name, Potion potion) {
        return register(Unicopia.id(name), potion);
    }

    static Holder<Potion> register(Identifier id, Potion potion) {
        return Registry.registerForHolder(BuiltInRegistries.POTION, id, potion);
    }

    record MorphingPotion(Identifier id, Holder<Potion> shortEffect, Holder<Potion> longEffect, Holder<Potion> permanentEffect) {
        public MorphingPotion(Race race) {
            this(race.getId(),
                    Objects.requireNonNull(MetamorphosisStatusEffect.forRace(race), "No metamorphosis status effect registered for " + race.getId()),
                    Objects.requireNonNull(RaceChangeStatusEffect.forRace(race), "No race change status effect registered for " + race.getId())
            );
        }

        public MorphingPotion(Identifier id, Holder<MobEffect> morphEffect, Holder<MobEffect> permanentEffect) {
            this(id,
                    register(id.withPath(p -> "short_morph_" + p), new Potion(id.getNamespace() + ".short_morph_" + id.getPath(), new MobEffectInstance(morphEffect, MetamorphosisStatusEffect.MAX_DURATION))),
                    register(id.withPath(p -> "long_morph_" + p), new Potion(id.getNamespace() + ".long_morph_" + id.getPath(), new MobEffectInstance(morphEffect, MetamorphosisStatusEffect.MAX_DURATION * 10))),
                    register(id, new Potion(id.getNamespace() + ".tribe_swap_" + id.getPath(), new MobEffectInstance(permanentEffect, RaceChangeStatusEffect.MAX_DURATION)))
            );
        }

    }

    static void bootstrap() {
        UEffects.bootstrap();
    }
}
