package com.minelittlepony.unicopia.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public abstract class FadeOutSoundInstance extends AbstractTickableSoundInstance {

    protected int transitionTicks = 10;

    private float sourceVolume;

    protected float targetVolume;

    private int prevProgress;
    private int progress;

    private boolean fadingOut;
    private boolean muted;
    private int ticksMuted;

    public FadeOutSoundInstance(SoundEvent sound, SoundSource category, float volume, RandomSource random) {
        super(sound, category, random);
        this.relative = true;
        this.looping = true;
        this.volume = volume;
        setTargetVolume(volume);
    }

    protected void setPosition(Vec3 pos) {
        x = pos.x;
        y = pos.y;
        z = pos.z;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        this.looping = !muted;
        if (!muted) {
            ticksMuted = 0;
        }
    }

    @Override
    public final void tick() {
        boolean fadeOut = !shouldKeepPlaying();

        if (!muted) {
            setFadingOut(fadeOut);
        } else if (ticksMuted++ > 2000) {
            stop();
            return;
        }

        prevProgress = progress;
        if (progress < transitionTicks) {
            progress++;
        }

        if (fadingOut && getVolume() < 0.001F) {
            stop();
        }
    }

    private void setFadingOut(boolean fadingOut) {
        if (fadingOut == this.fadingOut) {
            return;
        }

        this.fadingOut = fadingOut;
        setTargetVolume(fadingOut ? 0 : volume);
    }

    protected abstract boolean shouldKeepPlaying();

    protected void setTargetVolume(float target) {
        sourceVolume = getLerpedVolume();
        targetVolume = target;
        progress = 0;
    }

    protected void setVolume(float vol){
        sourceVolume = vol;
        targetVolume = vol;
        progress = 0;
    }

    private float getLerpedVolume() {
        float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float interpolate = Mth.clamp(Mth.lerpInt(delta, prevProgress, progress) / transitionTicks, 0, 1);
        return Mth.lerp(interpolate, sourceVolume, targetVolume);
    }

    @Override
    public final float getVolume() {
        if (muted) {
            return 0.001F;
        }
        return getLerpedVolume() * sound.getVolume().sample(random);
    }
}
