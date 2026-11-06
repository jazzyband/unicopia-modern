package com.minelittlepony.unicopia.util.serialization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import io.netty.buffer.ByteBuf;

public interface PacketCodecUtils {
    StreamCodec<FriendlyByteBuf, ResourceKey<?>> REGISTRY_KEY = StreamCodec.composite(
            Identifier.STREAM_CODEC, ResourceKey::registry,
            Identifier.STREAM_CODEC, ResourceKey::identifier,
            (registry, value) -> ResourceKey.create(ResourceKey.createRegistryKey(registry), value)
    );
    StreamCodec<FriendlyByteBuf, Optional<ResourceKey<?>>> OPTIONAL_REGISTRY_KEY = ByteBufCodecs.optional(REGISTRY_KEY);
    StreamCodec<ByteBuf, Vec3> VECTOR = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x,
            ByteBufCodecs.DOUBLE, Vec3::y,
            ByteBufCodecs.DOUBLE, Vec3::z,
            Vec3::new
    );
    StreamCodec<ByteBuf, Optional<Vec3>> OPTIONAL_VECTOR = ByteBufCodecs.optional(VECTOR);
    StreamCodec<ByteBuf, Optional<BlockPos>> OPTIONAL_POS = ByteBufCodecs.optional(BlockPos.STREAM_CODEC);
    StreamCodec<ByteBuf, Set<String>> STRING_SET = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(HashSet::new));
    StreamCodec<ByteBuf, BlockState> BLOCK_STATE = ByteBufCodecs.INT.map(Block::stateById, Block::getId);

    Function<Class<?>, StreamCodec<ByteBuf, ?>> ENUM_CODEC_CACHE = Util.memoize(type -> {
        final Object[] values = type.getEnumConstants();
        return ByteBufCodecs.idMapper(i -> values[i], i -> ((Enum<?>)i).ordinal());
    });

    @SuppressWarnings("unchecked")
    static <T extends Enum<T>> StreamCodec<ByteBuf, T> ofEnum(Class<T> type) {
        return (StreamCodec<ByteBuf, T>)ENUM_CODEC_CACHE.apply(type);
    }

    static <B extends ByteBuf, K, V> StreamCodec.CodecOperation<B, V, Map<K, V>> toMap(Function<V, K> keyFunction) {
        return codec -> map(HashMap::new, codec, keyFunction, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, K, V, C extends Map<K, V>> StreamCodec<B, C> map(
            IntFunction<C> factory,
            StreamCodec<? super B, V> elementCodec,
            Function<V, K> keyFunction,
            int maxSize
        ) {
            return new StreamCodec<>() {
                @Override
                public C decode(B byteBuf) {
                    int i = ByteBufCodecs.readCount(byteBuf, maxSize);
                    C collection = factory.apply(Math.min(i, 65536));

                    for (int j = 0; j < i; j++) {
                        V v = elementCodec.decode(byteBuf);
                        K k = keyFunction.apply(v);
                        collection.put(k, v);
                    }

                    return collection;
                }

                @Override
                public void encode(B byteBuf, C collection) {
                    ByteBufCodecs.writeCount(byteBuf, collection.size(), maxSize);

                    for (V object : collection.values()) {
                        elementCodec.encode(byteBuf, object);
                    }
                }
            };
        }
}
