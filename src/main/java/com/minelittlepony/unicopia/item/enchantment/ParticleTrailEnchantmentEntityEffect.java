package com.minelittlepony.unicopia.item.enchantment;

import java.util.Optional;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.particle.WeakTarget;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ParticleTrailEnchantmentEntityEffect(
        Optional<ParticleOptions> particle,
        float followSpeed,
        int density,
        int probability
    ) implements EnchantmentEntityEffect {
    public static final MapCodec<ParticleTrailEnchantmentEntityEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ParticleTypes.CODEC.optionalFieldOf("particle").forGetter(ParticleTrailEnchantmentEntityEffect::particle),
            Codec.FLOAT.fieldOf("followSpeed").forGetter(ParticleTrailEnchantmentEntityEffect::followSpeed),
            Codec.INT.fieldOf("density").forGetter(ParticleTrailEnchantmentEntityEffect::density),
            Codec.INT.fieldOf("probability").forGetter(ParticleTrailEnchantmentEntityEffect::probability)
    ).apply(instance, ParticleTrailEnchantmentEntityEffect::new));

    @Override
    public MapCodec<ParticleTrailEnchantmentEntityEffect> codec() {
        return CODEC;
    }

    @Override
    public void apply(ServerLevel world, int level, EnchantedItemInUse context, Entity entity, Vec3 pos) {
        if (probability <= 0 || world.getRandom().nextInt(probability) == 0) {
            ParticleUtils.spawnParticles(new FollowingParticleEffect(
                    UParticles.HEALTH_DRAIN,
                    new WeakTarget(entity.getEyePosition(1), entity), followSpeed, particle
            ), entity, density);
        }
    }
}
