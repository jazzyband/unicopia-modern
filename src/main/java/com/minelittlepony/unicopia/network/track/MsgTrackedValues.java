package com.minelittlepony.unicopia.network.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.sollace.fabwork.api.packets.Handled;

public record MsgTrackedValues(
        int owner,
        Optional<TrackerObjects> updatedObjects,
        Optional<TrackerEntries> updatedTrackers
) implements Handled<Player> {
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgTrackedValues> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MsgTrackedValues::owner,
            ByteBufCodecs.optional(TrackerObjects.PACKET_CODEC), MsgTrackedValues::updatedObjects,
            ByteBufCodecs.optional(TrackerEntries.PACKET_CODEC), MsgTrackedValues::updatedTrackers,
            MsgTrackedValues::new
    );

    @Override
    public void handle(Player sender) {
        Entity entity = sender.level().getEntity(owner);
        if (entity instanceof Trackable trackable) {
            trackable.getDataTrackers().load(this);
        }
    }

    public record TrackerObjects(int id, Set<UUID> removedValues, Map<UUID, byte[]> values) {
        public static final StreamCodec<RegistryFriendlyByteBuf, TrackerObjects> PACKET_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, TrackerObjects::id,
                UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), TrackerObjects::removedValues,
                ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.BYTE_ARRAY), TrackerObjects::values,
                TrackerObjects::new
        );
    }

    public record TrackerEntries(int id, boolean wipe, List<DataTracker.Pair<?>> values, Map<Integer, byte[]> objects) {
        public static final StreamCodec<RegistryFriendlyByteBuf, TrackerEntries> PACKET_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, TrackerEntries::id,
                ByteBufCodecs.BOOL, TrackerEntries::wipe,
                DataTracker.Pair.PACKET_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), TrackerEntries::values,
                ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, ByteBufCodecs.BYTE_ARRAY), TrackerEntries::objects,
                TrackerEntries::new
        );
    }
}
