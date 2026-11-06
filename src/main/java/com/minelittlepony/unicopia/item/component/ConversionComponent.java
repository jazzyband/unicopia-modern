package com.minelittlepony.unicopia.item.component;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import com.mojang.serialization.Codec;

public record ConversionComponent(ResourceKey<Item> convertsInto) {
    public static final Codec<ConversionComponent> CODEC = ResourceKey.codec(Registries.ITEM).xmap(ConversionComponent::new, ConversionComponent::convertsInto);
    public static final StreamCodec<RegistryFriendlyByteBuf, ConversionComponent> PACKET_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.ITEM), ConversionComponent::convertsInto,
            ConversionComponent::new
    );

    public Optional<Item> getItem() {
        return BuiltInRegistries.ITEM.getOptional(convertsInto());
    }
}
