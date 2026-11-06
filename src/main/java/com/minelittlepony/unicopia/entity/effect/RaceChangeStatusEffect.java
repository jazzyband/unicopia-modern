package com.minelittlepony.unicopia.entity.effect;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.MagicReserves;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.ExplosionUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.Level;

public class RaceChangeStatusEffect extends MobEffect {
    public static final int STAGE_DURATION = 200;
    public static final int MAX_DURATION = Stage.VALUES.length * STAGE_DURATION + 1;

    private static final Map<Race, Holder<MobEffect>> REGISTRY = new HashMap<>();

    @Nullable
    public static Holder<MobEffect> forRace(Race race) {
        return REGISTRY.get(race);
    }

    public static final Holder<MobEffect> EARTH = register(0x886F0F, Race.EARTH);
    public static final Holder<MobEffect> UNICORN = register(0x88FFFF, Race.UNICORN);
    public static final Holder<MobEffect> PEGASUS = register(0x00C0ff, Race.PEGASUS);
    public static final Holder<MobEffect> BAT = register(0x152F13, Race.BAT);
    public static final Holder<MobEffect> CHANGELING = register(0xFFFF00, Race.CHANGELING);
    public static final Holder<MobEffect> KIRIN = register(0xFF8800, Race.KIRIN);
    public static final Holder<MobEffect> HIPPOGRIFF = register(0xE04F77, Race.HIPPOGRIFF);

    private final Race race;

    public static Holder<MobEffect> register(int color, Race race) {
        Identifier id = race.getId();
        Holder<MobEffect> reference = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                id.withPath(p -> "change_race_" + id.getPath()),
                (MobEffect)new RaceChangeStatusEffect(color, race)
        );
        REGISTRY.put(race, reference);
        return reference;
    }

    private RaceChangeStatusEffect(int color, Race race) {
        super(MobEffectCategory.NEUTRAL, color);
        this.race = race;
    }

    public Race getSpecies() {
        return race;
    }

    private void resetTicks(LivingEntity entity) {
        Pony.of(entity).ifPresent(pony -> pony.setTicksmetamorphising(0));
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        MobEffectInstance state = entity.getEffect(forRace(race));

        if (state == null || entity.isDeadOrDying()) {
            resetTicks(entity);
            return false;
        }

        Equine<?> eq = Equine.of(entity).orElse(null);

        if (eq == null) {
            return false;
        }

        int metaTicks = 0;

        if (eq instanceof Pony pony) {
            metaTicks = pony.getTicksMetamorphising() + 1;
            pony.setTicksmetamorphising(metaTicks);
        }

        int ticks = Math.max(0, MAX_DURATION - state.getDuration());

        Stage stage = Stage.forDuration(ticks / STAGE_DURATION);

        if (stage == Stage.INITIAL) {
            return true;
        }

        int progression = ticks % (stage.ordinal() * STAGE_DURATION);

        if (eq.getSpecies() == race || !race.isPermitted(entity instanceof Player player ? player : null)) {
            if (progression == 0 && entity instanceof Player player && stage == Stage.CRAWLING) {
                player.sendOverlayMessage(Stage.INITIAL.getMessage(race));
            }
            return false;
        }

        if (progression == 0 && stage != Stage.DEATH && entity instanceof Player player) {
            player.sendOverlayMessage(stage.getMessage(race));
        }

        if (eq instanceof Pony pony) {
            MagicReserves magic = pony.getMagicalReserves();
            pony.setRespawnRace(race);
            magic.getExertion().add(5);
            if (magic.getEnergy().get() < 1) {
                magic.getEnergy().add(1.1F);
                entity.playSound(USounds.BLOCK_CHITIN_AMBIENCE, 0.1F, 2F);
            }
            magic.getExhaustion().add(3);

            if (state.isVisible()) {
                pony.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, 1);
            }
        }

        if (stage == Stage.DEATH) {
            if (eq instanceof Caster) {
                ((Caster<?>)eq).getSpellSlot().clear();
            }

            if (eq instanceof Pony pony && metaTicks > 200) {
                MagicReserves magic = pony.getMagicalReserves();
                magic.getEnergy().set(0.6F);
                magic.getExhaustion().set(0);
                magic.getExertion().set(0);

                if (pony.asEntity().isCreative() || entity.level().getDifficulty().getId() < 2 || entity.level().getLevelData().isHardcore()) {

                    if (!pony.asEntity().isCreative()) {
                        float cost = entity.level().getLevelData().isHardcore() ? 0.75F : switch (entity.level().getDifficulty()) {
                            case PEACEFUL -> 0.125F;
                            case EASY -> 0.25F;
                            default -> 0.5F;
                        };

                        entity.setHealth(Math.max(1, entity.getHealth() * cost));
                        FoodData hunger = pony.asEntity().getFoodData();
                        int food = hunger.getFoodLevel();
                        pony.asEntity().getFoodData().setFoodLevel(Math.max(Math.min(1, food), (int)(food * cost)));
                    }
                    pony.setSpecies(race);
                } else if (!pony.asEntity().isCreative()) {
                    if (!entity.hurtServer(world, Living.living(entity).damageOf(UDamageTypes.TRIBE_SWAP), Float.MAX_VALUE)) {
                        entity.setHealth(0);
                        pony.setRespawnRace(Race.UNSET);
                        pony.setSpecies(race);
                    }
                }
                entity.level().explode(entity, null, ExplosionUtil.NON_DESTRUCTIVE, entity.position(), 5, true, ExplosionInteraction.MOB);
            } else {
                eq.setSpecies(race);
                entity.level().explode(entity, null, ExplosionUtil.NON_DESTRUCTIVE, entity.position(), 5, true, ExplosionInteraction.MOB);
            }

            return false;
        }

        return true;
    }

    @Override
    public void applyInstantaneousEffect(ServerLevel world, @Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        applyEffectTick(world, target, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0;
    }

    enum Stage {
        INITIAL,
        CRAWLING,
        DETERMINATION,
        RESURECTION,
        DEATH;

        static Stage[] VALUES = values();

        public static Stage forDuration(int duration) {
            return VALUES[duration % VALUES.length];
        }

        public String getTranslationKey() {
            return String.format("unicopia.effect.tribe.stage.%s", name().toLowerCase());
        }

        public Component getMessage(Race race) {
            return Component.translatable(getTranslationKey(), race.getDisplayName());
        }
    }
}
