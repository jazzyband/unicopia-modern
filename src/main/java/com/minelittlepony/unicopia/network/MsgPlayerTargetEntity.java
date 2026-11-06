package com.minelittlepony.unicopia.network;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.sollace.fabwork.api.packets.Handled;

public record MsgPlayerTargetEntity(Optional<Integer> entityId, Optional<LevitatingItemEntity.Action> action, Optional<Vec3> direction) implements Handled<Player> {
    public static final StreamCodec<FriendlyByteBuf, MsgPlayerTargetEntity> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.INT), MsgPlayerTargetEntity::entityId,
            ByteBufCodecs.optional(PacketCodecUtils.ofEnum(LevitatingItemEntity.Action.class)), MsgPlayerTargetEntity::action,
            ByteBufCodecs.optional(PacketCodecUtils.VECTOR), MsgPlayerTargetEntity::direction,
            MsgPlayerTargetEntity::new
    );

    @Override
    public void handle(Player sender) {
        entityId.ifPresentOrElse(entityId -> {
            @Nullable
            Entity target = sender.level().getEntity(entityId);
            Pony.of(sender).setLookedEntity(target);
            if (target instanceof LevitatingItemEntity i) {
                action.ifPresent(action -> {
                    i.handleAction(action, sender, direction);
                });
            }
        }, () -> {
            Pony.of(sender).setLookedEntity(null);
        });

    }
}
