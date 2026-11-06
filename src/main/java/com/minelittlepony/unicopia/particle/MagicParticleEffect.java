package com.minelittlepony.unicopia.particle;

import org.joml.Vector3f;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public record MagicParticleEffect (
        boolean tinted,
        Vector3f color
    ) implements ParticleOptions {
    public static final MagicParticleEffect UNICORN = new MagicParticleEffect(false, new Vector3f());
    public static final MapCodec<MagicParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("tinted").forGetter(MagicParticleEffect::tinted),
            ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(MagicParticleEffect::color)
    ).apply(instance, (tinted, color) -> new MagicParticleEffect(tinted, new Vector3f(color))));
    public static final StreamCodec<ByteBuf, MagicParticleEffect> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MagicParticleEffect::tinted,
            ByteBufCodecs.VECTOR3F, MagicParticleEffect::color,
            (tinted, color) -> new MagicParticleEffect(tinted, new Vector3f(color))
    );

    public MagicParticleEffect(int tint) {
        this(true, new Vector3f(ARGB.redFloat(tint), ARGB.greenFloat(tint), ARGB.blueFloat(tint)));
    }

    public MagicParticleEffect(Vector3f color) {
        this(true, color);
    }

    public Vector3f getColor(RandomSource random) {
        if (tinted()) {
            return color;
        }

        float r = random.nextBoolean() ? 0.9F : 1;
        float g = 0.3F;
        float b = random.nextBoolean() ? 0.4F : 1;

        if (random.nextBoolean()) {
            g *= 2F;
        } else if (random.nextBoolean()) {
            r *= 3.9F;
        }

        return new Vector3f(r, g, b);
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.UNICORN_MAGIC;
    }
}
