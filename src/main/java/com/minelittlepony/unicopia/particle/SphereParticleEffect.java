package com.minelittlepony.unicopia.particle;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;

public record SphereParticleEffect (
        ParticleType<? extends SphereParticleEffect> type,
        Vector3f color,
        float alpha,
        float radius,
        Vec3 offset
    ) implements ParticleOptions {
    private static final Vec3 DEFAULT_OFFSET = new Vec3(0, 0.5, 0);
    public static MapCodec<SphereParticleEffect> createCodec(ParticleType<SphereParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(SphereParticleEffect::color),
            Codec.FLOAT.fieldOf("alpha").forGetter(SphereParticleEffect::alpha),
            Codec.FLOAT.fieldOf("radius").forGetter(SphereParticleEffect::radius),
            CodecUtils.VECTOR.fieldOf("offset").forGetter(SphereParticleEffect::offset)
        ).apply(instance, (color, alpha, radius, offset) -> new SphereParticleEffect(type, new Vector3f(color), alpha, radius, offset)));
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SphereParticleEffect> createPacketCodec(ParticleType<SphereParticleEffect> type) {
        return StreamCodec.composite(
                ByteBufCodecs.VECTOR3F, SphereParticleEffect::color,
                ByteBufCodecs.FLOAT, SphereParticleEffect::alpha,
                ByteBufCodecs.FLOAT, SphereParticleEffect::radius,
                PacketCodecUtils.VECTOR, SphereParticleEffect::offset,
                (color, alpha, radius, offset) -> new SphereParticleEffect(type, new Vector3f(color), alpha, radius, offset)
        );
    }

    public SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, int tint, float alpha, float rad) {
        this(type, tint, alpha, rad, DEFAULT_OFFSET);
    }

    public SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, Vector3f color, float alpha, float rad) {
        this(type, color, alpha, rad, DEFAULT_OFFSET);
    }

    public SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, int tint, float alpha, float rad, Vec3 offset) {
        this(type, new Vector3f(ARGB.red(tint), ARGB.green(tint), ARGB.blue(tint)), alpha, rad, offset);
    }

    public SphereParticleEffect withOffset(Vec3 offset) {
        return new SphereParticleEffect(type, color, alpha, radius, offset);
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}
