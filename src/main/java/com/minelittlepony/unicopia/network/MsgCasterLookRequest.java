package com.minelittlepony.unicopia.network;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import com.minelittlepony.unicopia.ability.data.Rot;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.OrientedSpell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;
import io.netty.buffer.ByteBuf;

/**
 * Sent to the client when the server needs to know precisely where the player is looking.
 */
public record MsgCasterLookRequest (UUID spellId) {
    public static final StreamCodec<ByteBuf, MsgCasterLookRequest> PACKET_CODEC = UUIDUtil.STREAM_CODEC.map(MsgCasterLookRequest::new, MsgCasterLookRequest::spellId);

    public record Reply (
            UUID spellId,
            Rot rotation
        ) implements Handled<ServerPlayer> {
        public static final StreamCodec<FriendlyByteBuf, Reply> PACKET_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, Reply::spellId,
                Rot.CODEC, Reply::rotation,
                Reply::new
        );

        public Reply(OrientedSpell spell, Caster<?> caster) {
            this(spell.getUuid(), Rot.of(caster));
        }

        @Override
        public void handle(ServerPlayer sender) {
            Pony pony = Pony.of(sender);
            pony.getSpellSlot()
                .get(SpellPredicate.IS_ORIENTED.withId(spellId))
                .ifPresent(spell -> spell.setOrientation(pony, rotation));
        }
    }
}
