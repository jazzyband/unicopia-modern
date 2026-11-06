package com.minelittlepony.unicopia.util.serialization;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public interface NbtSerialisable {
    /**
     * Called to save this to nbt to persist state on file or to transmit over the network
     *
     * @param compound  Compound tag to write to.
     */
    void toNBT(CompoundTag compound, Provider lookup);

    /**
     * Called to load this state from nbt
     *
     * @param compound  Compound tag to read from.
     */
    void fromNBT(CompoundTag compound, Provider lookup);

    default CompoundTag toNBT(Provider lookup) {
        CompoundTag compound = new CompoundTag();
        toNBT(compound, lookup);
        return compound;
    }

    @Deprecated
    static CompoundTag subTag(String name, CompoundTag parent) {
        CompoundTag child = new CompoundTag();
        parent.put(name, child);
        return child;
    }

    @Deprecated
    static CompoundTag subTag(String name, CompoundTag parent, Consumer<CompoundTag> writer) {
        writer.accept(subTag(name, parent));
        return parent;
    }

    @Deprecated
    static <K, V> Map<K, V> readMap(CompoundTag nbt, Function<String, K> keyFunction, BiFunction<K, Tag, V> valueFunction) {
        return nbt.keySet().stream().map(k -> {
            K key = keyFunction.apply(k);
            if (key == null) {
                return null;
            }
            V value = valueFunction.apply(key, nbt.get(k));
            if (value == null) {
                return null;
            }
            return Map.entry(key, value);
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Deprecated
    static <K, V> CompoundTag writeMap(Map<K, V> map, Function<K, String> keyFunction, Function<V, ? extends Tag> valueFunction) {
        return writeMap(new CompoundTag(), map, keyFunction, valueFunction);
    }

    @Deprecated
    private static <K, V> CompoundTag writeMap(CompoundTag nbt, Map<K, V> map, Function<K, String> keyFunction, Function<V, ? extends Tag> valueFunction) {
        map.forEach((k, v) -> nbt.put(keyFunction.apply(k), valueFunction.apply(v)));
        return nbt;
    }
}
