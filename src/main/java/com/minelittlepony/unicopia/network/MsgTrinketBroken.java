package com.minelittlepony.unicopia.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Sent to the client when an item equipped in a trinket slot breaks
 */
public record MsgTrinketBroken (ItemStack stack, int entityId) {
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgTrinketBroken> PACKET_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, MsgTrinketBroken::stack,
            ByteBufCodecs.INT, MsgTrinketBroken::entityId,
            MsgTrinketBroken::new
    );
}
