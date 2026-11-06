package com.minelittlepony.unicopia;

import static net.minecraft.sounds.SoundEvents.*;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public interface USounds {
    SoundEvent ENTITY_GENERIC_BUTTER_FINGERS = HONEY_BLOCK_SLIDE;

    SoundEvent ENTITY_PLAYER_CORRUPTION = register("entity.player.corrupt");
    SoundEvent ENTITY_PLAYER_BATPONY_SCREECH = register("entity.player.batpony.screech");
    SoundEvent ENTITY_PLAYER_HIPPOGRIFF_SCREECH = register("entity.player.hippogriff.screech");
    SoundEvent ENTITY_PLAYER_HIPPOGRIFF_PECK = CHICKEN_STEP.value();
    SoundEvent ENTITY_PLAYER_REBOUND = register("entity.player.rebound");
    SoundEvent ENTITY_PLAYER_PEGASUS_WINGSFLAP = register("entity.player.pegasus.wingsflap");
    SoundEvent ENTITY_PLAYER_PEGASUS_FLYING = register("entity.player.pegasus.flying");
    SoundEvent ENTITY_PLAYER_PEGASUS_DASH = register("entity.player.pegasus.dash");
    SoundEvent ENTITY_PLAYER_PEGASUS_MOLT = register("entity.player.pegasus.molt");
    SoundEvent ENTITY_PLAYER_EARTHPONY_DASH = RAVAGER_STEP;
    SoundEvent ENTITY_PLAYER_CHANGELING_BUZZ = register("entity.player.changeling.buzz");
    SoundEvent ENTITY_PLAYER_CHANGELING_TRANSFORM = register("entity.player.changeling.transform");
    Holder<SoundEvent> ENTITY_PLAYER_CHANGELING_FEED = GENERIC_DRINK;
    SoundEvent ENTITY_PLAYER_CHANGELING_CLIMB = CHICKEN_STEP.value();
    SoundEvent ENTITY_PLAYER_UNICORN_TELEPORT = register("entity.player.unicorn.teleport");
    SoundEvent ENTITY_PLAYER_KIRIN_RAGE = POLAR_BEAR_WARNING;
    SoundEvent ENTITY_PLAYER_KIRIN_RAGE_LOOP = register("entity.player.kirin.rage.loop");
    SoundEvent ENTITY_PLAYER_SEAPONY_SONAR = register("entity.player.seapony.sonar", 64);

    SoundEvent ENTITY_PLAYER_EARS_RINGING = register("entity.player.ears_ring");
    SoundEvent ENTITY_PLAYER_HEARTBEAT = register("entity.player.heartbeat");
    SoundEvent ENTITY_PLAYER_HEARTBEAT_LOOP = register("entity.player.heartbeat_loop");

    SoundEvent ENTITY_PLAYER_WOLOLO = register("entity.player.wololo");
    SoundEvent ENTITY_PLAYER_WHISTLE = register("entity.player.whistle");
    SoundEvent ENTITY_PLAYER_KICK = register("entity.player.kick");

    SoundEvent ENTITY_ARTEFACT_AMBIENT = register("entity.artefact.ambient");

    SoundEvent ENTITY_BUTTERFLY_HURT = register("entity.butterfly.hurt");

    SoundEvent ENTITY_TWITTERMITE_AMBIENT = register("entity.twittermite.ambient");
    SoundEvent ENTITY_TWITTERMITE_HURT = register("entity.twittermite.hurt");
    SoundEvent ENTITY_TWITTERMITE_DEATH = register("entity.twittermite.death");

    SoundEvent ENTITY_JAR_THROW = register("entity.jar.throw");

    SoundEvent ENTITY_CRYSTAL_HEART_ACTIVATE = register("entity.crystal_heart.activate");
    SoundEvent ENTITY_CRYSTAL_HEART_DEACTIVATE = register("entity.crystal_heart.deactivate");

    SoundEvent ENTITY_HOT_AIR_BALLOON_BOOST = GHAST_SHOOT;
    SoundEvent ENTITY_HOT_AIR_BALLOON_BURNER_FIRE = GHAST_SHOOT;
    SoundEvent ENTITY_HOT_AIR_BALLOON_STEP = WOOL_STEP;
    SoundEvent ENTITY_HOT_AIR_BALLOON_BASKET_STEP = BAMBOO_STEP;
    Holder<SoundEvent> ENTITY_HOT_AIR_BALLOON_EQUIP_CANOPY = ARMOR_EQUIP_LEATHER;
    SoundEvent ENTITY_HOT_AIR_BALLOON_EQUIP_BURNER = IRON_GOLEM_DAMAGE;

    SoundEvent ENTITY_SOMBRA_AMBIENT = register("entity.sombra.ambient");
    SoundEvent ENTITY_SOMBRA_LAUGH = register("entity.sombra.laugh");
    SoundEvent ENTITY_SOMBRA_SNICKER = register("entity.sombra.snicker");
    SoundEvent ENTITY_SOMBRA_SCARY = GHAST_AMBIENT;

    SoundEvent ENTITY_CRYSTAL_SHARDS_AMBIENT = AMETHYST_BLOCK_HIT;
    SoundEvent ENTITY_CRYSTAL_SHARDS_JOSTLE = AMETHYST_BLOCK_BREAK;

    SoundEvent ENTITY_IGNIMEOUS_BULB_HURT = WARDEN_HURT;
    SoundEvent ENTITY_IGNIMEOUS_BULB_DEATH = WARDEN_DEATH;

    SoundEvent ENTITY_TENTACLE_ROAR = RAVAGER_ROAR;
    SoundEvent ENTITY_TENTACLE_AMBIENT = CONDUIT_AMBIENT_SHORT;
    SoundEvent ENTITY_TENTACLE_DIG = WARDEN_DIG;

    SoundEvent ITEM_AMULET_CHARGING = register("item.amulet.charging");
    SoundEvent ITEM_AMULET_RECHARGE = register("item.amulet.recharge");

    SoundEvent ITEM_DRAGON_BREATH_SCROLL_USE = FIRECHARGE_USE;
    SoundEvent ITEM_DRAGON_BREATH_ARRIVE = FIRECHARGE_USE;

    SoundEvent ITEM_ICARUS_WINGS_PURIFY = register("item.icarus_wings.resonate");
    SoundEvent ITEM_ICARUS_WINGS_CORRUPT = register("item.icarus_wings.corrupted");
    SoundEvent ITEM_ICARUS_WINGS_WARN = register("item.icarus_wings.warn");
    SoundEvent ITEM_ICARUS_WINGS_EXHAUSTED = register("item.icarus_wings.break");

    SoundEvent ITEM_ALICORN_AMULET_CURSE = register("item.alicorn_amulet.curse");
    SoundEvent ITEM_ALICORN_AMULET_HALLUCINATION = register("item.alicorn_amulet.hallucination");
    SoundEvent ITEM_ALICORN_AMULET_AMBIENT = register("item.alicorn_amulet.ambient");

    SoundEvent ITEM_GROGAR_BELL_USE = BELL_BLOCK;
    SoundEvent ITEM_GROGAR_BELL_STOP_USING = BELL_BLOCK;
    SoundEvent ITEM_GROGAR_BELL_CHARGE = BELL_RESONATE;
    SoundEvent ITEM_GROGAR_BELL_DRAIN = GUARDIAN_ATTACK;

    SoundEvent ITEM_STAFF_STRIKE = PLAYER_ATTACK_CRIT;
    SoundEvent ITEM_MAGIC_STAFF_CHARGE = GUARDIAN_ATTACK;

    SoundEvent ITEM_CURING_JOKE_CURE = AMETHYST_BLOCK_BREAK;

    SoundEvent ITEM_ROCK_LAND = STONE_HIT;
    Holder.Reference<SoundEvent> ITEM_MUFFIN_BOUNCE = NOTE_BLOCK_BANJO;

    Holder<SoundEvent> ITEM_SUNGLASSES_SHATTER = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GLASS_BREAK);

    SoundEvent ITEM_APPLE_ROT = register("item.apple.rot");
    SoundEvent ITEM_BRACELET_SIGN = register("item.bracelet.sign");
    SoundEvent ITEM_MAGIC_AURA = register("item.magic.aura");

    SoundEvent BLOCK_CHITIN_AMBIENCE = register("block.chitin.ambience");
    SoundEvent BLOCK_SLIME_PUSTULE_POP = register("block.slime_pustule.pop");

    SoundEvent BLOCK_WEATHER_VANE_ROTATE = LANTERN_STEP;
    SoundEvent BLOCK_PIE_SLICE = BEEHIVE_SHEAR;
    SoundEvent BLOCK_PIE_SLICE_POP = ITEM_PICKUP;

    SoundEvent SPELL_CAST_FAIL = register("spell.cast.fail");
    SoundEvent SPELL_CAST_SUCCESS = register("spell.cast.success");
    SoundEvent SPELL_CAST_SHOOT = register("spell.cast.shoot");

    SoundEvent SPELL_ILLUSION_DISPERSE = register("spell.illusion.disperse");
    SoundEvent SPELL_FIRE_BOLT_SHOOT = register("spell.fire.shoot.bolt");
    SoundEvent SPELL_SHIELD_BURN_PROJECTILE = register("spell.shield.projectile.burn");
    SoundEvent SPELL_TRANSFORM_TRANSMUTE_ENTITY = register("spell.transform.transmute.entity");

    SoundEvent SPELL_AMBIENT = BEACON_AMBIENT;
    SoundEvent SPELL_MINDSWAP_SWAP = ZOMBIE_INFECT;
    SoundEvent SPELL_MINDSWAP_UNSWAP = ZOMBIE_VILLAGER_CURE;

    SoundEvent SPELL_BUBBLE_DISTURB = HONEY_BLOCK_STEP;
    SoundEvent SPELL_FIRE_CRACKLE = FURNACE_FIRE_CRACKLE;

    SoundEvent SPELL_NECROMANCY_ACTIVATE = BELL_BLOCK;

    SoundEvent SPELL_DISPLACEMENT_TELEPORT = HUSK_CONVERTED_TO_ZOMBIE;

    SoundEvent ENCHANTMENT_CONSUMPTION_CONSUME = PLAYER_BURP;

    SoundEvent PARTICLE_RAINBOOM_THUNDER = LIGHTNING_BOLT_THUNDER;
    SoundEvent PARTICLE_RAINBOOM_REVERB = LIGHTNING_BOLT_THUNDER;

    SoundEvent AMBIENT_WIND_GUST = register("ambient.wind.gust");
    SoundEvent AMBIENT_DARK_VORTEX_MOOD = register("ambient.dark_vortex.mood");
    SoundEvent AMBIENT_DARK_VORTEX_ADDITIONS = register("ambient.dark_vortex.additions");

    SoundEvent AMBIENT_ZAP_APPLE_RIPEN = register("ambient.zap_apple.ripen"); //USounds.Vanilla.ENTITY_WOLF_HOWL

    SoundEvent GUI_ABILITY_FAIL = register("gui.ability.fail");
    SoundEvent GUI_SPELL_CRAFT_SUCCESS = register("gui.spellcraft.success");
    Holder.Reference<SoundEvent> GUI_SPELL_EQUIP = UI_BUTTON_CLICK;

    Holder.Reference<SoundEvent> RECORD_CRUSADE = registerReference("music_disc.crusade");
    Holder.Reference<SoundEvent> RECORD_PET = registerReference("music_disc.pet");
    Holder.Reference<SoundEvent> RECORD_POPULAR = registerReference("music_disc.popular");
    Holder.Reference<SoundEvent> RECORD_FUNK = registerReference("music_disc.funk");

    static SoundEvent register(String name) {
        Identifier id = Unicopia.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    static Holder.Reference<SoundEvent> registerReference(String name) {
        Identifier id = Unicopia.id(name);
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    static SoundEvent register(String name, float range) {
        Identifier id = Unicopia.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createFixedRangeEvent(id, range));
    }

    static void bootstrap() {}

    static final class Vanilla extends SoundEvents {}
}
