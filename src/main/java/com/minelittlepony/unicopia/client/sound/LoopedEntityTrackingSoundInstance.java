package com.minelittlepony.unicopia.client.sound;

import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

public class LoopedEntityTrackingSoundInstance extends EntityBoundSoundInstance {
    public LoopedEntityTrackingSoundInstance(SoundEvent soundEvent, float volume, float pitch, Entity entity, long seed) {
        super(soundEvent, entity.getSoundSource(), volume, pitch, entity, seed);
        this.looping = true;
        this.delay = 0;
    }
}
