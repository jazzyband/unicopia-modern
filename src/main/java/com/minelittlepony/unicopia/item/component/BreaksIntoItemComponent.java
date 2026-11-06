package com.minelittlepony.unicopia.item.component;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BreaksIntoItemComponent(
        TagKey<DamageType> damageType,
        ResourceKey<Item> itemAfterBreaking,
        ResourceKey<SoundEvent> breakingSound
    ) {
    public static final Codec<BreaksIntoItemComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.hashedCodec(Registries.DAMAGE_TYPE).fieldOf("damage_type").forGetter(BreaksIntoItemComponent::damageType),
            ResourceKey.codec(Registries.ITEM).fieldOf("item_after_breaking").forGetter(BreaksIntoItemComponent::itemAfterBreaking),
            ResourceKey.codec(Registries.SOUND_EVENT).fieldOf("breaking_sound").forGetter(BreaksIntoItemComponent::breakingSound)
    ).apply(instance, BreaksIntoItemComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BreaksIntoItemComponent> PACKET_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.map(id -> TagKey.create(Registries.DAMAGE_TYPE, id), key -> key.location()), BreaksIntoItemComponent::damageType,
            ResourceKey.streamCodec(Registries.ITEM), BreaksIntoItemComponent::itemAfterBreaking,
            ResourceKey.streamCodec(Registries.SOUND_EVENT), BreaksIntoItemComponent::breakingSound,
            BreaksIntoItemComponent::new
    );

    public Optional<Item> getItemAfterBreaking() {
        return BuiltInRegistries.ITEM.getOptional(itemAfterBreaking());
    }

    public Optional<SoundEvent> getBreakingSound() {
        return BuiltInRegistries.SOUND_EVENT.getOptional(breakingSound);
    }
}
