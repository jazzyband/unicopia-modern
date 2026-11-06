package com.minelittlepony.unicopia.server.world;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

public interface UGameRules {
    GameRule<Boolean> SWAP_TRIBE_ON_DEATH = bool("swap_tribe_on_death", GameRuleCategory.SPAWNING, false);
    GameRule<Boolean> ANNOUNCE_TRIBE_JOINS = bool("announce_tribe_joins", GameRuleCategory.SPAWNING, false);
    GameRule<Boolean> DO_NOCTURNAL_BAT_PONIES = bool("do_nocturnal_bat_ponies", GameRuleCategory.PLAYER, true);
    GameRule<Integer> WEATHER_EFFECTS_STRENGTH = integer("weather_effects_strength", GameRuleCategory.MISC, 100, 0, 100);
    GameRule<Boolean> DO_TIME_MAGIC = bool("do_time_magic", GameRuleCategory.PLAYER, true);
    GameRule<Integer> MAX_DARK_VORTEX_SIZE = integer("max_dark_vortex_size", GameRuleCategory.PLAYER, 20, 1, 25);

    private static GameRule<Boolean> bool(String id, GameRuleCategory category, boolean defaultValue) {
        return Registry.register(BuiltInRegistries.GAME_RULE, Unicopia.id(id),
                new GameRule<>(category, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean,
                        Codec.BOOL, value -> value ? 1 : 0, defaultValue, FeatureFlagSet.of()));
    }

    private static GameRule<Integer> integer(String id, GameRuleCategory category, int defaultValue, int min, int max) {
        return Registry.register(BuiltInRegistries.GAME_RULE, Unicopia.id(id),
                new GameRule<>(category, GameRuleType.INT, IntegerArgumentType.integer(min, max), GameRuleTypeVisitor::visitInteger,
                        Codec.intRange(min, max), Integer::intValue, defaultValue, FeatureFlagSet.of()));
    }

    static void bootstrap() { }
}
