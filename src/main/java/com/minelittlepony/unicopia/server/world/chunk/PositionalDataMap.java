package com.minelittlepony.unicopia.server.world.chunk;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class PositionalDataMap<T extends PositionalDataMap.Hotspot> {
    private final Long2ObjectMap<Chunk<T>> chunks = new Long2ObjectOpenHashMap<>();
    private final Map<T, Set<Chunk<T>>> entryToChunks = new WeakHashMap<>();

    public Set<T> getState(BlockPos pos) {
        Chunk<T> chunk = chunks.get(ChunkPos.pack(pos));
        return chunk == null ? Set.of() : chunk.getState(pos);
    }

    @Nullable
    public Chunk<T> getChunk(ChunkPos pos) {
        return chunks.get(pos.pack());
    }

    public void remove(T entry) {
        Set<Chunk<T>> chunks = entryToChunks.remove(entry);
        if (chunks != null) {
            chunks.forEach(chunk -> chunk.remove(entry));
        }
    }

    public void update(T entry) {
        AABB entryBox = new AABB(entry.getCenter()).inflate(Mth.ceil(entry.radius()));
        Set<Chunk<T>> oldChunks = entryToChunks.get(entry);
        Set<Chunk<T>> newChunks = getIntersectingChunks(entryBox);
        if (oldChunks != null) {
            oldChunks.forEach(chunk -> chunk.remove(entry));
        }
        newChunks.forEach(chunk -> chunk.update(entry, entryBox));
        entryToChunks.put(entry, newChunks);
    }

    private Set<Chunk<T>> getIntersectingChunks(AABB entryBox) {
        int minX = SectionPos.posToSectionCoord(entryBox.minX);
        int maxX = SectionPos.posToSectionCoord(entryBox.maxX);
        int minZ = SectionPos.posToSectionCoord(entryBox.minZ);
        int maxZ = SectionPos.posToSectionCoord(entryBox.maxZ);

        Set<Chunk<T>> chunks = new HashSet<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                chunks.add(this.chunks.computeIfAbsent(ChunkPos.pack(x, z), Chunk::new));
            }
        }
        return chunks;
    }

    public interface Hotspot {
        float radius();

        BlockPos getCenter();
    }
}
