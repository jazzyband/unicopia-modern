package com.minelittlepony.unicopia;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.boat.Boat;

public interface EquinePredicates {
    Predicate<Entity> IS_PLAYER = e -> e instanceof Player;
    Predicate<Entity> BAT = physicalRaceMatches(Race.BAT::equals);
    Predicate<Entity> CHANGELING = physicalRaceMatches(Race.CHANGELING::equals);

    Predicate<Entity> RACE_CAN_INFLUENCE_WEATHER = raceMatches(Race::canInfluenceWeather);
    Predicate<Entity> RAGING = IS_PLAYER.and(SpellType.RAGE::isOn);

    Predicate<Entity> PLAYER_EARTH = IS_PLAYER.and(ofRace(Race.EARTH));
    Predicate<Entity> PLAYER_BAT = IS_PLAYER.and(BAT);
    Predicate<Entity> PLAYER_UNICORN = IS_PLAYER.and(raceMatches(Race::canCast));
    Predicate<Entity> PLAYER_CHANGELING = IS_PLAYER.and(ofRace(Race.CHANGELING));
    Predicate<Entity> PLAYER_KIRIN = IS_PLAYER.and(ofRace(Race.KIRIN));
    Predicate<Entity> PLAYER_SEAPONY = IS_PLAYER.and(raceMatches(Race::isFish));

    Predicate<Entity> PLAYER_CAN_USE_EARTH = IS_PLAYER.and(raceMatches(Race::canUseEarth));
    Predicate<Entity> IS_CASTER = e -> !e.isRemoved() && (e instanceof Caster || IS_PLAYER.test(e));
    Predicate<Entity> IS_PLACED_SPELL = e -> e instanceof Caster && !e.isRemoved();

    Predicate<Entity> IS_MAGIC_IMMUNE = EntitySelector.ENTITY_STILL_ALIVE.negate()
            .or(EntitySelector.NO_CREATIVE_OR_SPECTATOR.negate()
            .or(e -> (e instanceof MagicImmune || !(e instanceof LivingEntity))
            && !(e instanceof ItemEntity)
            && !(e instanceof ExperienceOrb)
            && !(e instanceof Boat)
            && !(e instanceof Projectile)));
    Predicate<Entity> EXCEPT_MAGIC_IMMUNE = IS_MAGIC_IMMUNE.negate();
    Predicate<Entity> VALID_LIVING_AND_NOT_MAGIC_IMMUNE = EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EXCEPT_MAGIC_IMMUNE);

    Predicate<LivingEntity> LIVING_HAS_WANT_IT_NEED_IT = e -> EnchantmentUtil.getWantItNeedItLevel(e) > 0;
    Predicate<Entity> VALID_FOR_DISGUISE = EntitySelector.NO_SPECTATORS.and(e -> !(e instanceof LightningBolt || e instanceof HangingEntity));

    static Predicate<Entity> ofRace(Race race) {
        return raceMatches(race::equals);
    }

    static Predicate<Entity> raceMatches(Predicate<Race> predicate) {
        return e -> Equine.of(e).filter(pony -> pony.getCompositeRace().any(predicate)).isPresent();
    }

    static Predicate<Entity> physicalRaceMatches(Predicate<Race> predicate) {
        return e -> Equine.of(e).filter(pony -> predicate.test(pony.getCompositeRace().physical())).isPresent();
    }
}
