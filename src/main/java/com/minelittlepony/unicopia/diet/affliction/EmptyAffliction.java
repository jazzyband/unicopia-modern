package com.minelittlepony.unicopia.diet.affliction;

import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record EmptyAffliction() implements Affliction {
    public static final EmptyAffliction INSTANCE = new EmptyAffliction();
    public static final MapCodec<EmptyAffliction> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, EmptyAffliction> PACKET_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void afflict(Player player, ItemStack stack) {
    }

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.EMPTY;
    }
}
