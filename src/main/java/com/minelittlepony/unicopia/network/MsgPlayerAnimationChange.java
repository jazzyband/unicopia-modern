package com.minelittlepony.unicopia.network;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import com.minelittlepony.unicopia.client.render.PlayerPoser.AnimationInstance;
import com.minelittlepony.unicopia.entity.player.Pony;

/**
 * Sent to the client when a player's animation changes.
 */
public record MsgPlayerAnimationChange (
        UUID playerId,
        AnimationInstance animation,
        int duration
    ) {
    public static final StreamCodec<FriendlyByteBuf, MsgPlayerAnimationChange> PACKET_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MsgPlayerAnimationChange::playerId,
            AnimationInstance.PACKET_CODEC, MsgPlayerAnimationChange::animation,
            ByteBufCodecs.INT, MsgPlayerAnimationChange::duration,
            MsgPlayerAnimationChange::new
    );

    public MsgPlayerAnimationChange(Pony player, AnimationInstance animation, int duration) {
        this(player.asEntity().getUUID(), animation, duration);
    }
}
