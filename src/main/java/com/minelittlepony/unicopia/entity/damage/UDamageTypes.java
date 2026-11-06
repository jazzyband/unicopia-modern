package com.minelittlepony.unicopia.entity.damage;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import com.minelittlepony.unicopia.Unicopia;

public interface UDamageTypes {
    List<ResourceKey<DamageType>> REGISTRY = new ArrayList<>();

    ResourceKey<DamageType> EXHAUSTION = register("magical_exhaustion");
    ResourceKey<DamageType> ALICORN_AMULET = register("alicorn_amulet");
    ResourceKey<DamageType> TRIBE_SWAP = register("tribe_swap");
    ResourceKey<DamageType> ZAP_APPLE = register("zap");
    ResourceKey<DamageType> KICK = register("kick");
    ResourceKey<DamageType> SMASH = register("smash");
    ResourceKey<DamageType> BAT_SCREECH = register("bat_screech");
    ResourceKey<DamageType> LOVE_DRAINING = register("love_draining");
    ResourceKey<DamageType> LIFE_DRAINING = register("life_draining");
    ResourceKey<DamageType> RAINBOOM = register("rainboom");
    ResourceKey<DamageType> STEAMROLLER = register("steamroller");
    ResourceKey<DamageType> GAVITY_WELL_RECOIL = register("gravity_well_recoil");
    ResourceKey<DamageType> SUN = register("sun");
    ResourceKey<DamageType> SUNLIGHT = register("sunlight");
    ResourceKey<DamageType> PETRIFIED = register("petrified");
    ResourceKey<DamageType> ROCK = register("rock");
    ResourceKey<DamageType> HORSESHOE = register("horseshoe");
    ResourceKey<DamageType> SPIKES = register("spikes");

    private static ResourceKey<DamageType> register(String name) {
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, Unicopia.id(name));
        REGISTRY.add(key);
        return key;
    }

    static void bootstrap() {}
}
