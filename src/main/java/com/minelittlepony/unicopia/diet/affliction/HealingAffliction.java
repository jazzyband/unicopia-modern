package com.minelittlepony.unicopia.diet.affliction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record HealingAffliction(float health) implements Affliction {
    public static final MapCodec<HealingAffliction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("health").forGetter(HealingAffliction::health)
    ).apply(instance, HealingAffliction::new));
    public static final StreamCodec<ByteBuf, HealingAffliction> PACKET_CODEC = ByteBufCodecs.FLOAT.map(HealingAffliction::new, HealingAffliction::health);

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.HEALING;
    }

    @Override
    public void afflict(Player player, ItemStack stack) {
        player.heal(health());
    }

    @Override
    public Component getName() {
        return Component.translatable(getType().getTranslationKey(), health());
    }
}
