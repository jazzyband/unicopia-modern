package com.minelittlepony.unicopia.entity.effect;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;

public class MetamorphosisStatusEffect extends MobEffect {
    public static final int MAX_DURATION = 20 * 60;

    private static final Map<Race, Holder<MobEffect>> REGISTRY = new HashMap<>();

    public static final Holder<MobEffect> EARTH = register(0x886F0F, Race.EARTH);
    public static final Holder<MobEffect> UNICORN = register(0x88FFFF, Race.UNICORN);
    public static final Holder<MobEffect> PEGASUS = register(0x00C0ff, Race.PEGASUS);
    public static final Holder<MobEffect> BAT = register(0x152F13, Race.BAT);
    public static final Holder<MobEffect> CHANGELING = register(0xFFFF00, Race.CHANGELING);
    public static final Holder<MobEffect> KIRIN = register(0xFF8800, Race.KIRIN);
    public static final Holder<MobEffect> HIPPOGRIFF = register(0xE04F77, Race.HIPPOGRIFF);

    @Nullable
    public static Holder<MobEffect> forRace(Race race) {
        return REGISTRY.get(race);
    }

    public static Holder<MobEffect> register(int color, Race race) {
        Identifier id = Race.REGISTRY.getKey(race);
        MobEffect effect = new MetamorphosisStatusEffect(color, race);
        var reference = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
                id.withPath(p -> "morph_race_" + p),
                effect
        );
        REGISTRY.put(race, reference);
        return reference;
    }

    public static Optional<Race> getEffectiveRace(LivingEntity entity) {
        return entity.getActiveEffects().stream().filter(effect -> effect.getEffect().value() instanceof MetamorphosisStatusEffect).map(effect -> {
            return ((MetamorphosisStatusEffect)effect.getEffect().value()).getRace();
        }).findFirst();
    }

    private final Race race;

    private MetamorphosisStatusEffect(int color, Race race) {
        super(MobEffectCategory.NEUTRAL, color);
        withSoundOnAdded(USounds.ENTITY_PLAYER_CHANGELING_TRANSFORM);
        this.race = race;
    }

    public Race getRace() {
        return race;
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);

        entity.getActiveEffects().stream()
            .filter(effect -> effect.getEffect().value() instanceof MetamorphosisStatusEffect && effect.getEffect().value() != this)
            .toList().forEach(effect -> entity.removeEffect(effect.getEffect()));

        ParticleUtils.spawnParticles(new MagicParticleEffect(getColor()), entity, 50);
    }
}
