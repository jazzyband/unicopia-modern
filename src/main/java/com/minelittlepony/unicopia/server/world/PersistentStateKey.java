package com.minelittlepony.unicopia.server.world;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import com.minelittlepony.unicopia.util.Untyped;
import com.mojang.serialization.Codec;

public final class PersistentStateKey<T extends SavedData> {
    private final Identifier id;
    private final Function<Level, T> constructor;
    private final Function<Level, SavedDataType<T>> typeFactory;
    private final Map<Level, WeakReference<SavedDataType<T>>> types = Collections.synchronizedMap(new WeakHashMap<>());

    public PersistentStateKey(Identifier id, Function<Level, Codec<T>> codec, Function<Level, T> factory) {
        this.id = id;
        this.constructor = factory;
        this.typeFactory = world -> new SavedDataType<>(id, () -> factory.apply(world), codec.apply(world), DataFixTypes.LEVEL);
    }

    public PersistentStateKey(Identifier id, Codec<T> codec, Supplier<T> factory) {
        this(id, world -> codec, world -> factory.get());
    }

    private SavedDataType<T> typeFor(Level world) {
        synchronized (types) {
            WeakReference<SavedDataType<T>> reference = types.get(world);
            SavedDataType<T> type = reference == null ? null : reference.get();
            if (type == null) {
                type = typeFactory.apply(world);
                types.put(world, new WeakReference<>(type));
            }
            return type;
        }
    }

    public T get(LevelReader world) {
        if (world instanceof ServerLevel serverWorld) {
            return serverWorld.getDataStorage().computeIfAbsent(typeFor(serverWorld));
        }

        return ClientInstance.of((Level)world, id, constructor).instance();
    }

    record ClientInstance<T extends SavedData>(WeakReference<Level> world, T instance) {
        private static final Map<Identifier, ClientInstance<?>> INSTANCES = new HashMap<>();

        public static <T extends SavedData> ClientInstance<T> of(Level world, Identifier id, Function<Level, T> factory) {
            return Untyped.cast(INSTANCES.compute(id, (i, instance) -> {
                if (instance == null || !instance.matches(world)) {
                    return new ClientInstance<>(world, factory);
                }
                return instance;
            }));
        }

        public ClientInstance(Level world, Function<Level, T> factory) {
            this(new WeakReference<>(world), factory.apply(world));
        }

        public boolean matches(Level world) {
            return world().get() == world;
        }
    }
}
