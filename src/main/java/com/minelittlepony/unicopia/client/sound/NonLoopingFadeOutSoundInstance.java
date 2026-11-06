package com.minelittlepony.unicopia.client.sound;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class NonLoopingFadeOutSoundInstance extends FadeOutSoundInstance {

    private final long duration;
    private long ticks;

    public NonLoopingFadeOutSoundInstance(SoundEvent sound, SoundSource category, float volume, RandomSource random, long duration) {
        super(sound, category, volume, random);
        this.duration = duration;
    }

    @Override
    protected boolean shouldKeepPlaying() {
        return ticks++ < duration;
    }

}
