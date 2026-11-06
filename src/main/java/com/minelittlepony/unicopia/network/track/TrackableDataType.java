package com.minelittlepony.unicopia.network.track;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.SkinFeatures;
import com.minelittlepony.unicopia.util.Untyped;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public record TrackableDataType<T>(int id, StreamCodec<RegistryFriendlyByteBuf, TypedValue<T>> codec) {
    private static final Int2ObjectMap<TrackableDataType<?>> REGISTRY = new Int2ObjectOpenHashMap<>();
    public static final StreamCodec<RegistryFriendlyByteBuf, TypedValue<?>> PACKET_CODEC = ByteBufCodecs.INT.<RegistryFriendlyByteBuf>cast().map(
            id -> Objects.requireNonNull(REGISTRY.get(id.intValue()), "Unknown trackable data type id: " + id),
            type -> type.id()
    ).dispatch(value -> Untyped.cast(value.type), TrackableDataType::codec);

    public static final TrackableDataType<Integer> INT = of(Identifier.parse("integer"), ByteBufCodecs.INT);
    public static final TrackableDataType<Float> FLOAT = of(Identifier.parse("float"), ByteBufCodecs.FLOAT);
    public static final TrackableDataType<Boolean> BOOLEAN = of(Identifier.parse("boolean"), ByteBufCodecs.BOOL);
    public static final TrackableDataType<Optional<UUID>> UUID = of(Identifier.parse("uuid"), ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC));
    public static final TrackableDataType<CompoundTag> NBT = of(Identifier.parse("nbt"), ByteBufCodecs.COMPOUND_TAG);
    public static final TrackableDataType<CompoundTag> COMPRESSED_NBT = of(Identifier.parse("compressed_nbt"), ByteBufCodecs.COMPOUND_TAG);

    public static final TrackableDataType<Optional<BlockPos>> OPTIONAL_POS = of(Identifier.parse("optional_pos"), PacketCodecUtils.OPTIONAL_POS);
    public static final TrackableDataType<Optional<Vec3>> OPTIONAL_VECTOR = of(Identifier.parse("optional_vector"), PacketCodecUtils.OPTIONAL_VECTOR);
    private static final TrackableDataType<Optional<ResourceKey<?>>> OPTIONAL_REGISTRY_KEY = of(Identifier.parse("optional_registry_key"), PacketCodecUtils.OPTIONAL_REGISTRY_KEY);

    public static final TrackableDataType<Race> RACE = of(Unicopia.id("race"), ByteBufCodecs.registry(Race.REGISTRY_KEY));
    public static final TrackableDataType<SkinFeatures> SKIN_FEATURES = of(Unicopia.id("skin_features"), SkinFeatures.PACKET_CODEC);

    public static <T> TrackableDataType<Optional<ResourceKey<T>>> ofRegistryKey() {
        return Untyped.cast(OPTIONAL_REGISTRY_KEY);
    }

    public static <T> TrackableDataType<T> of(Identifier typeName, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        return Untyped.cast(REGISTRY.computeIfAbsent(typeName.hashCode(), t -> {
            AtomicReference<TrackableDataType<T>> ref = new AtomicReference<>();
            ref.set(new TrackableDataType<>(t, codec.<RegistryFriendlyByteBuf>cast().map(
                    value -> new TypedValue<>(ref.get(), value),
                    value -> value.value
            )));
            return ref.get();
        }));
    }

    public static class TypedValue<T> {
        final TrackableDataType<T> type;
        public T value;

        public TypedValue(TrackableDataType<T> type, T value) {
            this.type = type;
            this.value = value;
        }
    }
}
