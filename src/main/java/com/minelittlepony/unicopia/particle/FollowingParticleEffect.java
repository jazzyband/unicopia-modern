package com.minelittlepony.unicopia.particle;

import java.util.Optional;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FollowingParticleEffect (
        ParticleType<FollowingParticleEffect> type,
        WeakTarget target,
        float followSpeed,
        Optional<ParticleOptions> childEffect
    ) implements ParticleOptions {
    public static MapCodec<FollowingParticleEffect> createCodec(ParticleType<FollowingParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            WeakTarget.CODEC.fieldOf("target").forGetter(FollowingParticleEffect::target),
            Codec.FLOAT.fieldOf("follow_speed").forGetter(FollowingParticleEffect::followSpeed),
            ParticleTypes.CODEC.optionalFieldOf("child_effect").forGetter(FollowingParticleEffect::childEffect)
        ).apply(instance, (target, speed, effect) -> new FollowingParticleEffect(type, target, speed, effect)));
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, FollowingParticleEffect> createPacketCodec(ParticleType<FollowingParticleEffect> type) {
        return StreamCodec.composite(
                WeakTarget.PACKET_CODEC, FollowingParticleEffect::target,
                ByteBufCodecs.FLOAT, FollowingParticleEffect::followSpeed,
                ByteBufCodecs.optional(ParticleTypes.STREAM_CODEC), FollowingParticleEffect::childEffect,
                (target, speed, effect) -> new FollowingParticleEffect(type, target, speed, effect)
        );
    }

    public FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, Vec3 target, float followSpeed) {
        this(type, new WeakTarget(target, (Entity)null), followSpeed, Optional.empty());
    }

    public FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, Entity target, float followSpeed) {
        this(type, new WeakTarget(target.getEyePosition(1), target), followSpeed, Optional.empty());
    }

    public FollowingParticleEffect withChild(ParticleOptions child) {
        return new FollowingParticleEffect(type, target, followSpeed, Optional.of(child));
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}
