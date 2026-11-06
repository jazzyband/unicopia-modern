package com.minelittlepony.unicopia.diet;

import com.minelittlepony.unicopia.diet.affliction.Affliction;
import com.minelittlepony.unicopia.diet.affliction.AfflictionType;
import com.minelittlepony.unicopia.diet.affliction.EmptyAffliction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record Ailment(Affliction effects) {
    public static final Ailment EMPTY = new Ailment(EmptyAffliction.INSTANCE);
    public static final Codec<Ailment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AfflictionType.CODEC.fieldOf("effects").forGetter(Ailment::effects)
    ).apply(instance, Ailment::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Ailment> PACKET_CODEC = AfflictionType.PACKET_CODEC.map(Ailment::new, Ailment::effects);
}
