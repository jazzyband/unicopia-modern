package com.minelittlepony.unicopia.util;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.base.Predicates;
import com.mojang.serialization.Lifecycle;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.util.Util;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;

public interface RegistryUtils {
    static <T> Registry<T> createSimple(Identifier id) {
        return FabricRegistryBuilder.from(new MappedRegistry<T>(ResourceKey.createRegistryKey(id), Lifecycle.stable())).buildAndRegister();
    }

    static <T> Registry<T> createSynced(Identifier id) {
        return FabricRegistryBuilder.from(new MappedRegistry<T>(ResourceKey.createRegistryKey(id), Lifecycle.stable()))
                .attribute(RegistryAttribute.SYNCED)
                .buildAndRegister();
    }

    static <T> Registry<T> createSynced(Identifier id, String def) {
        return FabricRegistryBuilder.from(new DefaultedMappedRegistry<T>(def, ResourceKey.createRegistryKey(id), Lifecycle.stable(), false))
                .attribute(RegistryAttribute.SYNCED)
                .buildAndRegister();
    }

    @SuppressWarnings("unchecked")
    static <T> HolderSet<T> entriesForTag(Level world, TagKey<T> key) {
        return world.registryAccess().lookupOrThrow(key.registry())
                .get(key)
                .map(HolderSet.class::cast)
                .orElse(HolderSet.<T>empty());
    }

    static <T> Stream<T> valuesForTag(Level world, TagKey<T> key) {
        return entriesForTag(world, key).stream().map(Holder::value);
    }

    static <T> Optional<T> pickRandom(Level world, TagKey<T> key) {
        return pickRandomEntry(world, key).map(Holder::value);
    }

    static <T> Optional<Holder<T>> pickRandomEntry(Level world, TagKey<T> key) {
        return pickRandomEntry(world, key, Predicates.alwaysTrue());
    }

    static <T> Optional<T> pickRandom(Level world, TagKey<T> key, Predicate<T> filter) {
        return Util.getRandomSafe(entriesForTag(world, key).stream().map(Holder::value).filter(filter).toList(), world.getRandom());
    }

    static <T> Optional<Holder<T>> pickRandomEntry(Level world, TagKey<T> key, Predicate<Holder<T>> filter) {
        return Util.getRandomSafe(entriesForTag(world, key).stream().filter(filter).toList(), world.getRandom());
    }

    static <T> boolean isIn(Level world, T obj, ResourceKey<? extends Registry<T>> registry, TagKey<T> tag) {
        return world.registryAccess().lookupOrThrow(registry).wrapAsHolder(obj).is(tag);
    }

    static <T> Identifier getId(Level world, T obj, ResourceKey<? extends Registry<T>> registry) {
        return world.registryAccess().lookupOrThrow(registry).getKey(obj);
    }
}
