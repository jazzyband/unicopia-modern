package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.function.Function;

import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

/** Bridges registered objects used by Unicopia's generators to 26.3 registry keys. */
final class RegistryEntryTagAppender<T> implements TagAppender<T> {
    private final TagAppender<T> delegate;
    private final Function<T, ResourceKey<T>> keyOf;
    private final Function<Identifier, ResourceKey<T>> keyForId;

    RegistryEntryTagAppender(TagAppender<T> delegate, Function<T, ResourceKey<T>> keyOf,
            Function<Identifier, ResourceKey<T>> keyForId) {
        this.delegate = delegate;
        this.keyOf = keyOf;
        this.keyForId = keyForId;
    }

    @SafeVarargs
    final RegistryEntryTagAppender<T> add(T... entries) {
        for (T entry : entries) {
            delegate.add(keyOf.apply(entry));
        }
        return this;
    }

    RegistryEntryTagAppender<T> addOptional(Identifier id) {
        delegate.addOptional(keyForId.apply(id));
        return this;
    }

    RegistryEntryTagAppender<T> addOptionalTag(Identifier id) {
        delegate.addOptionalTag(TagKey.create(keyForId.apply(id).registryKey(), id));
        return this;
    }

    @Override
    public RegistryEntryTagAppender<T> add(ResourceKey<T> key) {
        delegate.add(key);
        return this;
    }

    @Override
    public RegistryEntryTagAppender<T> addOptional(ResourceKey<T> key) {
        delegate.addOptional(key);
        return this;
    }

    @Override
    public RegistryEntryTagAppender<T> addTag(TagKey<T> tag) {
        delegate.addTag(tag);
        return this;
    }

    @Override
    public RegistryEntryTagAppender<T> addOptionalTag(TagKey<T> tag) {
        delegate.addOptionalTag(tag);
        return this;
    }

    @Override
    public RegistryEntryTagAppender<T> forceAddTag(TagKey<T> tag) {
        delegate.forceAddTag(tag);
        return this;
    }

    @Override
    public RegistryEntryTagAppender<T> setReplace(boolean replace) {
        delegate.setReplace(replace);
        return this;
    }

    @Override
    public TagBuilder getBuilder() {
        return delegate.getBuilder();
    }
}
