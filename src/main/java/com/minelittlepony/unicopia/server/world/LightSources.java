package com.minelittlepony.unicopia.server.world;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.saveddata.SavedData;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.DynamicLightSource;
import com.mojang.serialization.Codec;
import com.mojang.datafixers.util.Unit;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class LightSources extends SavedData {
    private static final Identifier ID = Unicopia.id("light_sources");
    private static final PersistentStateKey<LightSources> KEY = new PersistentStateKey<>(ID,
            world -> Codec.EMPTY.codec().xmap(unit -> new LightSources(world), sources -> Unit.INSTANCE),
            LightSources::new);

    private final Object2LongMap<UUID> lightSourceLocations = new Object2LongOpenHashMap<>();
    private final Long2ObjectMap<ObjectSet<UUID>> lightSources = new Long2ObjectOpenHashMap<>();
    private final Object2IntMap<UUID> lightSourceClientIds = new Object2IntOpenHashMap<>();

    private volatile boolean empty = true;

    private final Level world;

    private final Function<UUID, Entity> entitySupplier;

    public static LightSources get(LevelReader world) {
        return KEY.get(world);
    }

    LightSources(Level world) {
        this.world = world;
        entitySupplier = world instanceof ServerLevel s ? s::getEntity : id -> world.getEntity(lightSourceClientIds.getInt(id));
    }

    public <T extends Entity & DynamicLightSource> void addLightSource(T entity) {
        synchronized (lightSources) {
            lightSourceLocations.computeLong(entity.getUUID(), (id, lastPos) -> {
                lightSourceClientIds.put(id, entity.getId());
                long pos = entity.getLightSourcePosition().asLong();
                if (lastPos == null || lastPos != pos) {
                    if (lastPos != null) {
                        lightSources.get(lastPos.longValue()).remove(id);
                        checkBlock(lastPos.longValue());
                    }
                    lightSources.computeIfAbsent(pos, l -> new ObjectOpenHashSet<>()).add(id);
                    checkBlock(pos);
                }
                return pos;
            });
            empty = false;
        }
    }

    private void checkBlock(long pos) {
        try {
            world.getLightEngine().checkBlock(BlockPos.of(pos));
        } catch (Exception ignored) { }
    }

    public void removeLightSource(UUID id) {
        if (empty) {
            return;
        }
        synchronized (lightSources) {
            lightSourceClientIds.removeInt(id);
            if (lightSourceLocations.containsKey(id)) {
                lightSources.computeIfPresent(lightSourceLocations.removeLong(id), (p, ids) -> {
                    ids.remove(id);
                    checkBlock(p);
                    return ids.isEmpty() ? null : ids;
                });
            }
            empty = lightSources.isEmpty();
        }
    }

    public int getLuminance(long blockPos) {
        if (empty) {
            return 0;
        }

        int result = 0;
        synchronized (lightSources) {
            ObjectSet<UUID> ids = lightSources.get(blockPos);
            if (ids != null) {
                final Iterator<UUID> each = ids.iterator();
                while (each.hasNext()) {
                    UUID id = each.next();
                    int level = getLightLevel(id);
                    if (level > 0) {
                        result += level;
                        if (result >= 15) {
                            break;
                        }
                    } else {
                        each.remove();
                        lightSourceClientIds.removeInt(id);
                        lightSourceLocations.removeLong(id);
                    }
                }
                if (ids.isEmpty()) {
                    lightSources.remove(blockPos);
                }
            }
        }
        return Math.min(result, 15);
    }

    public void forEachLightSource(ChunkPos chunkPos, LightSourceConsumer consumer) {
        if (empty) {
            return;
        }

        synchronized (lightSources) {
            lightSources.long2ObjectEntrySet().removeIf(entry -> {
                if (checkPos(chunkPos, entry.getLongKey())) {
                    int result = 0;
                    final Iterator<UUID> each = entry.getValue().iterator();
                    while (each.hasNext()) {
                        UUID id = each.next();
                        int level = getLightLevel(id);
                        if (level > 0) {
                            result += level;
                            if (result >= 15) {
                                break;
                            }
                        } else {
                            each.remove();
                            lightSourceClientIds.removeInt(id);
                            lightSourceLocations.removeLong(id);
                        }
                    }

                    consumer.accept(entry.getLongKey(), Math.min(result, 15));
                }
                return entry.getValue().isEmpty();
            });
            empty = lightSources.isEmpty();
        }
    }

    private int getLightLevel(UUID id) {
        Entity entity = entitySupplier.apply(id);
        if (entity instanceof DynamicLightSource source && !entity.isRemoved()) {
            return source.getLightLevel();
        }
        return 0;
    }

    private boolean checkPos(ChunkPos chunkPos, long pos) {
        return world.isOutsideBuildHeight(BlockPos.getY(pos))
            && checkPos(BlockPos.getX(pos), chunkPos.getMinBlockX(), chunkPos.getMaxBlockX())
            && checkPos(BlockPos.getZ(pos), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockZ());
    }

    private boolean checkPos(int p, int min, int max) {
        return p >= min && p <= max;
    }

    public interface LightSourceConsumer {
        void accept(long pos, int light);
    }
}
