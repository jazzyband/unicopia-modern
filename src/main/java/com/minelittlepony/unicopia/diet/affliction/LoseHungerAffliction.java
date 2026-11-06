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

public record LoseHungerAffliction(float multiplier) implements Affliction {
    public static final MapCodec<LoseHungerAffliction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("multiplier").forGetter(LoseHungerAffliction::multiplier)
    ).apply(instance, LoseHungerAffliction::new));
    public static final StreamCodec<ByteBuf, LoseHungerAffliction> PACKET_CODEC = ByteBufCodecs.FLOAT.map(LoseHungerAffliction::new, LoseHungerAffliction::multiplier);

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.LOSE_HUNGER;
    }

    @Override
    public void afflict(Player player, ItemStack stack) {
        var hunger = player.getFoodData();
        hunger.setFoodLevel((int)(hunger.getFoodLevel() * multiplier));
        hunger.setSaturation(hunger.getSaturationLevel() * multiplier);
    }

    @Override
    public Component getName() {
        return Component.translatable(getType().getTranslationKey(), multiplier * 100);
    }
}
