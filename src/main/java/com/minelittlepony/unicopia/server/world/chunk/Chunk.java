package com.minelittlepony.unicopia.server.world.chunk;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.AABB;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class Chunk<T extends PositionalDataMap.Hotspot> {
    private final Long2ObjectMap<Section<T>> sections = new Long2ObjectOpenHashMap<>();
    private final Map<T, Set<Section<T>>> entryToSections = new WeakHashMap<>();

    Chunk(long pos) { }

    public synchronized Set<T> getState(BlockPos pos) {
        Section<T> section = sections.get(SectionPos.blockToSectionCoord(pos.getY()));
        return section == null ? Set.of() : section.getState(pos);
    }

    public synchronized Set<T> getStates() {
        return new HashSet<>(entryToSections.keySet());
    }

    public synchronized boolean remove(T entry) {
        Set<Section<T>> sections = entryToSections.remove(entry);
        if (sections != null) {
            sections.forEach(section -> {
                if (section.remove(entry) && section.isEmpty()) {
                    this.sections.remove(section.pos);
                }
            });
            return true;
        }
        return false;
    }

    public synchronized boolean update(T entry, AABB entryBox) {
        Set<Section<T>> oldSections = entryToSections.get(entry);
        Set<Section<T>> newSections = getIntersectingSections(entryBox);
        if (oldSections != null) {
            oldSections.forEach(section -> {
                if (!newSections.contains(section) && section.remove(entry) && section.isEmpty()) {
                    this.sections.remove(section.pos);
                }
            });
        }
        newSections.forEach(chunk -> chunk.update(entry, entryBox));
        entryToSections.put(entry, newSections);
        return true;
    }

    private Set<Section<T>> getIntersectingSections(AABB entryBox) {
        Set<Section<T>> sections = new HashSet<>();

        int minY = SectionPos.posToSectionCoord(entryBox.minY);
        int maxY = SectionPos.posToSectionCoord(entryBox.maxY);
        for (int y = minY; y <= maxY; y++) {
            sections.add(this.sections.computeIfAbsent(y, Section::new));
        }

        return sections;
    }
}
