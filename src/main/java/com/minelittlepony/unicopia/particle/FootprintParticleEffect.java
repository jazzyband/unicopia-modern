package com.minelittlepony.unicopia.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FootprintParticleEffect (
        float yaw
    ) implements ParticleOptions {
    public static final MapCodec<FootprintParticleEffect> CODEC = Codec.FLOAT.fieldOf("yaw").xmap(FootprintParticleEffect::new, FootprintParticleEffect::yaw);
    public static final StreamCodec<ByteBuf, FootprintParticleEffect> PACKET_CODEC = ByteBufCodecs.FLOAT.map(FootprintParticleEffect::new, FootprintParticleEffect::yaw);

    @Override
    public ParticleType<?> getType() {
        return UParticles.FOOTPRINT;
    }
}
