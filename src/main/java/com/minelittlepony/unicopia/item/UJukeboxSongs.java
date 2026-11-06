package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

public interface UJukeboxSongs {
    ResourceKey<JukeboxSong> CRUSADE = of("crusade");
    ResourceKey<JukeboxSong> PET = of("pet");
    ResourceKey<JukeboxSong> POPULAR = of("popular");
    ResourceKey<JukeboxSong> FUNK = of("funk");

    private static ResourceKey<JukeboxSong> of(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, Unicopia.id(name));
    }
}
