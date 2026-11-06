package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record AmbientSoundsEnchantmentEffect (
        Identifier id,
        TagKey<SoundEvent> sounds
) implements EnchantmentEntityEffect {
    public static final MapCodec<AmbientSoundsEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(AmbientSoundsEnchantmentEffect::id),
            TagKey.hashedCodec(Registries.SOUND_EVENT).fieldOf("sounds").forGetter(AmbientSoundsEnchantmentEffect::sounds)
    ).apply(instance, AmbientSoundsEnchantmentEffect::new));

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }

    @Override
    public void apply(ServerLevel world, int level, EnchantedItemInUse context, Entity entity, Vec3 pos) {
        Living.getOrEmpty(entity).ifPresent(user -> {
            user.getEnchants().compute(id, (id, data) -> {
                if (data == null) {
                    data = 0F;
                }
                RandomSource rng = world.getRandom();
                data -= rng.nextFloat() * 0.8F;
                int light = world.getMaxLocalRawBrightness(entity.getRootVehicle().blockPosition());
                if (rng.nextInt(Math.max(1, (light * 9) + data.intValue())) == 0) {
                    data = (float)rng.nextInt(5000);

                    RegistryUtils.pickRandom(world, sounds).ifPresent(event -> {
                        user.asWorld().playSound(
                                null,
                                user.asEntity(),
                                event, SoundSource.HOSTILE,
                                0.5F + rng.nextFloat() * 0.5F,
                                0.5F + rng.nextFloat() * 0.5F
                        );
                    });
                }
                return data;
            });
        });

    }
}
