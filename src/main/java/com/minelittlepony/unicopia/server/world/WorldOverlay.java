package com.minelittlepony.unicopia.server.world;

import java.util.List;
import java.util.function.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.Tickable;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class WorldOverlay<T extends WorldOverlay.State> extends SavedData implements Tickable {

    private final Level world;

    private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();

    private final Object locker = new Object();

    private final Supplier<T> factory;
    @Nullable
    private final BiConsumer<Long2ObjectMap<T>, List<ServerPlayer>> updateSender;

    public static <T extends State> PersistentStateKey<WorldOverlay<T>> createAccessor(Identifier id, Supplier<T> factory, @Nullable BiConsumer<Long2ObjectMap<T>, List<ServerPlayer>> updateSender) {
        return createAccessor(id, w -> new WorldOverlay<>(w, factory, updateSender));
    }

    public static  <T extends State> PersistentStateKey<WorldOverlay<T>> createAccessor(Identifier id, Function<Level, WorldOverlay<T>> overlayFactory) {
        return new PersistentStateKey<>(id, world -> {
            return CompoundTag.CODEC.xmap(tag -> {
                WorldOverlay<T> overlay = overlayFactory.apply(world);
                overlay.readNbt(tag, world.registryAccess());
                return overlay;
            }, overlay -> {
                return overlay.writeNbt(new CompoundTag(), world.registryAccess());
            });
        }, overlayFactory);
    }

    WorldOverlay(Level world, Supplier<T> factory, @Nullable BiConsumer<Long2ObjectMap<T>, List<ServerPlayer>> updateSender) {
        this.world = world;
        this.factory = factory;
        this.updateSender = updateSender;
    }

    public CompoundTag writeNbt(CompoundTag compound, Provider lookup) {
        CompoundTag destructions = new CompoundTag();
        this.chunks.forEach((id, chunk) -> {
            destructions.put(Long.toString(id), chunk.toNBT(lookup));
        });
        compound.put("chunks", destructions);
        return compound;
    }

    public void readNbt(CompoundTag compound, Provider lookup) {
        CompoundTag d = compound.getCompoundOrEmpty("chunks");
        d.keySet().forEach(id -> {
            chunks.computeIfAbsent(Long.valueOf(id), Chunk::new).fromNBT(d.getCompoundOrEmpty(id), lookup);
        });
    }

    @Nullable
    public T getState(BlockPos pos) {
        return getChunk(pos).getState(pos);
    }

    public T getOrCreateState(BlockPos pos) {
        synchronized (locker) {
            return getChunk(pos).getOrCreateState(pos);
        }
    }

    private Chunk getChunk(BlockPos pos) {
        return chunks.computeIfAbsent(ChunkPos.pack(pos), Chunk::new);
    }

    public void setState(BlockPos pos, @Nullable T state) {
        synchronized (locker) {
            getChunk(pos).setState(pos, state);
            setDirty();
        }
    }

    @Override
    public void tick() {
        synchronized (locker) {
            chunks.long2ObjectEntrySet().removeIf(entry -> entry.getValue().tick());

            if (world instanceof ServerLevel sw) {
                chunks.forEach((chunkPos, chunk) -> chunk.sendUpdates(sw));
            }
        }
    }

    private class Chunk implements NbtSerialisable {
        private final Long2ObjectMap<T> states = new Long2ObjectOpenHashMap<>();

        private final long pos;

        Chunk(long pos) {
            this.pos = pos;
        }

        @Nullable
        public T getState(BlockPos pos) {
            return states.get(pos.asLong());
        }

        public T getOrCreateState(BlockPos pos) {
            return states.computeIfAbsent(pos.asLong(), l -> factory.get());
        }

        public void setState(BlockPos pos, @Nullable T state) {
            if (state == null) {
                states.remove(pos.asLong());
            } else {
                states.put(pos.asLong(), state);
            }
        }

        boolean tick() {
            states.long2ObjectEntrySet().removeIf(e -> e.getValue().tick());
            return states.isEmpty();
        }

        void sendUpdates(ServerLevel world) {
            if (updateSender == null) {
                return;
            }

            if (!world.getChunkSource().hasChunk(ChunkPos.getX(pos), ChunkPos.getZ(pos))) {
                return;
            }

            ChunkMap storage = world.getChunkSource().chunkMap;

            List<ServerPlayer> players = storage.getPlayers(ChunkPos.unpack(pos), false);

            if (!players.isEmpty()) {
                updateSender.accept(states, players);
            }
        }

        @Override
        public void toNBT(CompoundTag compound, Provider lookup) {
            CompoundTag states = new CompoundTag();
            this.states.forEach((id, state) -> {
                states.put(Long.toString(id), state.toNBT(lookup));
            });
            compound.put("states", states);
        }

        @Override
        public void fromNBT(CompoundTag compound, Provider lookup) {
            CompoundTag d = compound.getCompoundOrEmpty("states");
            chunks.clear();
            d.keySet().forEach(id -> {
                states.computeIfAbsent(Long.valueOf(id), i -> factory.get()).fromNBT(d.getCompoundOrEmpty(id), lookup);
            });
        }
    }

    public interface State extends NbtSerialisable {
        boolean tick();
    }
}
