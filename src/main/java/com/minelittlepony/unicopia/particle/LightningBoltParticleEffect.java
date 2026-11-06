package com.minelittlepony.unicopia.particle;

import java.util.Optional;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LightningBoltParticleEffect (
        boolean silent,
        int changeFrequency,
        int maxBranches,
        float maxDeviation,
        Optional<Vec3> pathEndPoint
    ) implements ParticleOptions {
    public static final LightningBoltParticleEffect DEFAULT = new LightningBoltParticleEffect(false, 10, 6, 3, Optional.empty());

    public static final MapCodec<LightningBoltParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("silent").forGetter(LightningBoltParticleEffect::silent),
            Codec.INT.fieldOf("changeFrequency").forGetter(LightningBoltParticleEffect::changeFrequency),
            Codec.INT.fieldOf("maxBranches").forGetter(LightningBoltParticleEffect::maxBranches),
            Codec.FLOAT.fieldOf("maxDeviation").forGetter(LightningBoltParticleEffect::maxDeviation),
            CodecUtils.VECTOR.optionalFieldOf("pathEndPoint").forGetter(LightningBoltParticleEffect::pathEndPoint)
    ).apply(instance, LightningBoltParticleEffect::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, LightningBoltParticleEffect> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, LightningBoltParticleEffect::silent,
            ByteBufCodecs.INT, LightningBoltParticleEffect::changeFrequency,
            ByteBufCodecs.INT, LightningBoltParticleEffect::maxBranches,
            ByteBufCodecs.FLOAT, LightningBoltParticleEffect::maxDeviation,
            PacketCodecUtils.OPTIONAL_VECTOR, LightningBoltParticleEffect::pathEndPoint,
            LightningBoltParticleEffect::new
    );

    @Override
    public ParticleType<?> getType() {
        return UParticles.LIGHTNING_BOLT;
    }
}
