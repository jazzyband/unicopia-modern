package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;

public record MsgUnlockTraits (Set<Trait> traits) {
    public static final StreamCodec<FriendlyByteBuf, MsgUnlockTraits> PACKET_CODEC = StreamCodec.composite(
            Trait.PACKET_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), MsgUnlockTraits::traits,
            MsgUnlockTraits::new
    );

    public MsgUnlockTraits(Set<Trait> traits) {
        this.traits = new HashSet<>(traits);
    }
}
