package com.minelittlepony.unicopia.datagen;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.resources.Identifier;

public class DataCollector<T> {
    private final HashMap<Identifier, T> values = new HashMap<>();

    private final PathProvider resolver;
    private final Function<T, JsonElement> jsonConversionFunction;

    public DataCollector(PathProvider resolver, Function<T, JsonElement> jsonConversionFunction) {
        this.resolver = resolver;
        this.jsonConversionFunction = jsonConversionFunction;
    }

    public DataCollector(PathProvider resolver, Codec<T> codec) {
        this(resolver, t -> codec.encodeStart(JsonOps.INSTANCE, t).getOrThrow());
    }

    public boolean isDefined(Identifier id) {
        return values.containsKey(id);
    }

    public Stream<T> streamValues() {
        return values.values().stream();
    }

    public <V> Consumer<V> prime(BiConsumer<V, BiConsumer<Identifier, T>> converter) {
        var consumer = prime();
        return element -> converter.accept(element, consumer);
    }

    public BiConsumer<Identifier, T> prime() {
        values.clear();
        return (Identifier id, T value) ->
            Preconditions.checkState(values.put(id, value) == null, "Duplicate model definition for " + id);
    }

    public CompletableFuture<?> upload(CachedOutput cache) {
        return CompletableFuture.allOf(values.entrySet()
                .stream()
                .map(entry -> DataProvider.saveStable(cache, jsonConversionFunction.apply(entry.getValue()), resolver.json(entry.getKey())))
                .toArray(CompletableFuture[]::new)
        );
    }

    public interface Identifiable {
        Identifier getId();
    }
}
