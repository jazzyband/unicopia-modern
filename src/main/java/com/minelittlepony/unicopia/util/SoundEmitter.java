package com.minelittlepony.unicopia.util;

import com.minelittlepony.unicopia.EntityConvertable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface SoundEmitter<E extends Entity> extends EntityConvertable<E> {

    default void playSound(SoundEvent sound, float volume, float pitch) {
        playSoundAt(asEntity(), sound, volume, pitch);
    }

    default void playSound(SoundEvent sound, float volume) {
        playSound(sound, volume, getRandomPitch());
    }

    default float getRandomPitch() {
        return getRandomPitch(asEntity().level().getRandom());
    }

    static void playSoundAt(Entity entity, SoundEvent sound, float volume, float pitch) {
        playSoundAt(entity, sound, entity.getSoundSource(), volume, pitch);
    }

    static void playSoundAt(Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch) {
        if (entity.level().isClientSide() && entity instanceof Player p) {
            entity.level().playSound(p, entity.getX(), entity.getY(), entity.getZ(), sound, category, volume, pitch);
        } else {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, category, volume, pitch);
        }
    }

    static float getRandomPitch(RandomSource rng) {
        return (float)rng.triangle(0.5F, 0.2F);
    }
}
