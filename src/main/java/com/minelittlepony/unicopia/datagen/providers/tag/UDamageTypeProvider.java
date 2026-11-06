package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

public class UDamageTypeProvider extends FabricTagsProvider<DamageType> {
    public UDamageTypeProvider(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
        super(output, Registries.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    protected void addTags(Provider lookup) {
        tag(UTags.DamageTypes.SPELLBOOK_IMMUNE_TO).add(
                UDamageTypes.ZAP_APPLE, UDamageTypes.LOVE_DRAINING, UDamageTypes.LIFE_DRAINING,
                UDamageTypes.RAINBOOM, UDamageTypes.SUN, UDamageTypes.SUNLIGHT, UDamageTypes.SMASH
        ).forceAddTag(DamageTypeTags.IS_FALL).forceAddTag(DamageTypeTags.IS_FREEZING).forceAddTag(DamageTypeTags.IS_LIGHTNING).forceAddTag(DamageTypeTags.IS_PROJECTILE);
        tag(UTags.DamageTypes.FROM_ROCKS).add(UDamageTypes.ROCK);
        tag(UTags.DamageTypes.FROM_HORSESHOES).add(UDamageTypes.HORSESHOE);
        tag(UTags.DamageTypes.BREAKS_SUNGLASSES).add(
                UDamageTypes.BAT_SCREECH, UDamageTypes.RAINBOOM,
                DamageTypes.FLY_INTO_WALL, DamageTypes.FALLING_ANVIL, DamageTypes.FALLING_BLOCK,
                DamageTypes.SONIC_BOOM
        ).forceAddTag(DamageTypeTags.IS_EXPLOSION);

        tag(DamageTypeTags.AVOIDS_GUARDIAN_THORNS).add(
                UDamageTypes.EXHAUSTION, UDamageTypes.ALICORN_AMULET, UDamageTypes.ZAP_APPLE, UDamageTypes.KICK, UDamageTypes.SMASH,
                UDamageTypes.BAT_SCREECH, UDamageTypes.LOVE_DRAINING, UDamageTypes.LIFE_DRAINING, UDamageTypes.RAINBOOM, UDamageTypes.STEAMROLLER
        );
        tag(DamageTypeTags.BYPASSES_ARMOR).add(
                UDamageTypes.EXHAUSTION, UDamageTypes.GAVITY_WELL_RECOIL, UDamageTypes.ALICORN_AMULET,
                UDamageTypes.ZAP_APPLE, UDamageTypes.KICK, UDamageTypes.SMASH, UDamageTypes.BAT_SCREECH,
                UDamageTypes.LOVE_DRAINING, UDamageTypes.LIFE_DRAINING, UDamageTypes.STEAMROLLER, UDamageTypes.RAINBOOM,
                UDamageTypes.SUN, UDamageTypes.SUNLIGHT, UDamageTypes.PETRIFIED, UDamageTypes.TRIBE_SWAP
        );
        tag(DamageTypeTags.BYPASSES_INVULNERABILITY).add(UDamageTypes.TRIBE_SWAP);
        tag(DamageTypeTags.BYPASSES_COOLDOWN).add(UDamageTypes.TRIBE_SWAP);
        tag(DamageTypeTags.BYPASSES_EFFECTS).add(UDamageTypes.TRIBE_SWAP);
        tag(DamageTypeTags.BYPASSES_RESISTANCE).add(UDamageTypes.TRIBE_SWAP);
        tag(DamageTypeTags.BYPASSES_ENCHANTMENTS).add(UDamageTypes.TRIBE_SWAP);
        tag(DamageTypeTags.BYPASSES_SHIELD).add(
                UDamageTypes.EXHAUSTION, UDamageTypes.BAT_SCREECH, UDamageTypes.ALICORN_AMULET,
                UDamageTypes.LOVE_DRAINING, UDamageTypes.LIFE_DRAINING,
                UDamageTypes.RAINBOOM, UDamageTypes.TRIBE_SWAP
        );
        tag(DamageTypeTags.IS_FIRE).add(UDamageTypes.SUN, UDamageTypes.SUNLIGHT, UDamageTypes.PETRIFIED);
        tag(DamageTypeTags.IS_LIGHTNING).add(UDamageTypes.ZAP_APPLE);
        tag(DamageTypeTags.WITCH_RESISTANT_TO).add(
                UDamageTypes.EXHAUSTION, UDamageTypes.ALICORN_AMULET, UDamageTypes.ZAP_APPLE,
                UDamageTypes.LOVE_DRAINING, UDamageTypes.LIFE_DRAINING,
                UDamageTypes.KICK, UDamageTypes.SMASH, UDamageTypes.STEAMROLLER
        );
    }
}
