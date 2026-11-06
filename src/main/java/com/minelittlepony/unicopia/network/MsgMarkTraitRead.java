package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;

public record MsgMarkTraitRead (Set<Trait> traits) implements Handled<ServerPlayer> {
    public static final StreamCodec<FriendlyByteBuf, MsgMarkTraitRead> PACKET_CODEC = StreamCodec.composite(
            Trait.PACKET_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), MsgMarkTraitRead::traits,
            MsgMarkTraitRead::new
    );

    @Override
    public void handle(ServerPlayer sender) {
        Pony.of(sender).getDiscoveries().markRead(traits);
    }
}
