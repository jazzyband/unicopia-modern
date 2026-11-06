package com.minelittlepony.unicopia.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SpiralParticleEffect(
        WeakTarget centerPoint,
        float angularVelocity,
        ParticleOptions effect
    ) implements ParticleOptions {
    public static final MapCodec<SpiralParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WeakTarget.CODEC.fieldOf("centerPoint").forGetter(SpiralParticleEffect::centerPoint),
            Codec.FLOAT.fieldOf("angularVelocity").forGetter(SpiralParticleEffect::angularVelocity),
            ParticleTypes.CODEC.fieldOf("effect").forGetter(SpiralParticleEffect::effect)
    ).apply(instance, SpiralParticleEffect::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpiralParticleEffect> PACKET_CODEC = StreamCodec.composite(
            WeakTarget.PACKET_CODEC, SpiralParticleEffect::centerPoint,
            ByteBufCodecs.FLOAT, SpiralParticleEffect::angularVelocity,
            ParticleTypes.STREAM_CODEC, SpiralParticleEffect::effect,
            SpiralParticleEffect::new
    );

    @Override
    public ParticleType<?> getType() {
        return UParticles.SPIRAL;
    }
}
