package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class USoundEventTagProvider extends FabricTagsProvider<SoundEvent> {
    public USoundEventTagProvider(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
        super(output, Registries.SOUND_EVENT, registriesFuture);
    }

    @Override
    protected void addTags(Provider lookup) {
        tag(UTags.Sounds.POISON_JOKE_EVENTS).add(
                SoundEvents.AMBIENT_CAVE.key(),
                SoundEvents.MUSIC_MENU.key(),
                SoundEvents.SHIELD_BREAK.key(),
                SoundEvents.GENERIC_EXPLODE.key(),
                SoundEvents.AMBIENT_NETHER_WASTES_LOOP.key(),
                SoundEvents.AMBIENT_NETHER_WASTES_MOOD.key(),
                SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD.key(),
                SoundEvents.AMBIENT_BASALT_DELTAS_MOOD.key(),
                SoundEvents.AMBIENT_BASALT_DELTAS_LOOP.key()
        ).add(
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.CREEPER_PRIMED)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.STONE_BUTTON_CLICK_ON)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.STONE_BUTTON_CLICK_OFF)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.WOODEN_DOOR_CLOSE)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.STONE_BREAK)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.BLAZE_AMBIENT)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.ZOMBIE_AMBIENT)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.DROWNED_AMBIENT)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.ENDERMITE_AMBIENT)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.SKELETON_AMBIENT)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.SKELETON_HORSE_AMBIENT)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.ZOGLIN_AMBIENT)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.TNT_PRIMED)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE)),
                ResourceKey.create(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE))
        );
    }
}
