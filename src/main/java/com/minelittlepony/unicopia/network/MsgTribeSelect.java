package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import com.minelittlepony.unicopia.Race;

public record MsgTribeSelect (Set<Race> availableRaces, String serverMessage) {
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgTribeSelect> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Race.REGISTRY_KEY).apply(ByteBufCodecs.collection(HashSet::new)), MsgTribeSelect::availableRaces,
            ByteBufCodecs.STRING_UTF8, MsgTribeSelect::serverMessage,
            MsgTribeSelect::new
    );
}
