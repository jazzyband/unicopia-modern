package com.minelittlepony.unicopia.particle;


import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public record TargetBoundParticleEffect (
        ParticleType<TargetBoundParticleEffect> type,
        int targetId
    ) implements ParticleOptions {
    public static MapCodec<TargetBoundParticleEffect> createCodec(ParticleType<TargetBoundParticleEffect> type) {
        return Codec.INT.fieldOf("targetId").xmap(targetId -> new TargetBoundParticleEffect(type, targetId), TargetBoundParticleEffect::targetId);
    }

    public static final StreamCodec<ByteBuf, TargetBoundParticleEffect> createPacketCodec(ParticleType<TargetBoundParticleEffect> type) {
        return ByteBufCodecs.INT.map(targetId -> new TargetBoundParticleEffect(type, targetId), TargetBoundParticleEffect::targetId);
    }

    public TargetBoundParticleEffect(ParticleType<TargetBoundParticleEffect> type, @Nullable Entity target) {
        this(type, target == null ? -1 : target.getId());
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}
