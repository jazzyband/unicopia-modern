package com.minelittlepony.unicopia.network;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;
import io.netty.buffer.ByteBuf;

/**
 * Sent to the server when a player dismisses a spell from their dismiss spell screen
 */
public record MsgRemoveSpell (UUID id) implements Handled<ServerPlayer> {
    public static final StreamCodec<ByteBuf, MsgRemoveSpell> PACKET_CODEC = UUIDUtil.STREAM_CODEC.map(MsgRemoveSpell::new, MsgRemoveSpell::id);

    public MsgRemoveSpell(Spell spell) {
        this(spell.getUuid());
    }

    @Override
    public void handle(ServerPlayer sender) {
        Pony player = Pony.of(sender);
        if (player != null) {
            player.getSpellSlot().remove(id);
        }
    }
}
