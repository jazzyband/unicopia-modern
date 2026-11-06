package com.minelittlepony.unicopia.datagen.providers;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.item.UJukeboxSongs;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;

public class UJukeboxSongProvider extends FabricDynamicRegistryProvider {
    public UJukeboxSongProvider(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Jukebox Songs";
    }

    @Override
    protected void configure(Provider registries, Entries entries) {
        register(entries, UJukeboxSongs.CRUSADE, USounds.RECORD_CRUSADE, 181, 1);
        register(entries, UJukeboxSongs.PET, USounds.RECORD_PET, 221, 2);
        register(entries, UJukeboxSongs.POPULAR, USounds.RECORD_POPULAR, 112, 3);
        register(entries, UJukeboxSongs.FUNK, USounds.RECORD_FUNK, 91, 4);
    }

    private void register(
            Entries entries,
            ResourceKey<JukeboxSong> key,
            Holder.Reference<SoundEvent> soundEvent, int lengthInSeconds, int comparatorOutput) {
        entries.add(key, new JukeboxSong(soundEvent, Component.translatable(Util.makeDescriptionId("jukebox_song", key.identifier())), lengthInSeconds, comparatorOutput));
    }

}
