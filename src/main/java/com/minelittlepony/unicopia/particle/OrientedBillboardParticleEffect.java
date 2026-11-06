package com.minelittlepony.unicopia.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record OrientedBillboardParticleEffect (
        ParticleType<OrientedBillboardParticleEffect> type,
        boolean fixed,
        float yaw,
        float pitch
    ) implements ParticleOptions {
    public static MapCodec<OrientedBillboardParticleEffect> createCodec(ParticleType<OrientedBillboardParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("fixed").forGetter(OrientedBillboardParticleEffect::fixed),
            Codec.FLOAT.fieldOf("yaw").forGetter(OrientedBillboardParticleEffect::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(OrientedBillboardParticleEffect::pitch)
        ).apply(instance, (fixed, yaw, pitch) -> new OrientedBillboardParticleEffect(type, fixed, yaw, pitch)));
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, OrientedBillboardParticleEffect> createPacketCodec(ParticleType<OrientedBillboardParticleEffect> type) {
        return StreamCodec.composite(
                ByteBufCodecs.BOOL, OrientedBillboardParticleEffect::fixed,
                ByteBufCodecs.FLOAT, OrientedBillboardParticleEffect::yaw,
                ByteBufCodecs.FLOAT, OrientedBillboardParticleEffect::pitch,
                (fixed, yaw, pitch) -> new OrientedBillboardParticleEffect(type, fixed, yaw, pitch)
        );
    }

    public OrientedBillboardParticleEffect(ParticleType<OrientedBillboardParticleEffect> type, Vec3 orientation) {
        this(type, (float)orientation.x(), (float)orientation.y());
    }

    public OrientedBillboardParticleEffect(ParticleType<OrientedBillboardParticleEffect> type, float yaw, float pitch) {
        this(type, true, yaw, pitch);
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}
