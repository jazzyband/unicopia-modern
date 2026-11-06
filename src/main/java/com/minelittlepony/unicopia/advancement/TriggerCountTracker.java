package com.minelittlepony.unicopia.advancement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.Copyable;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class TriggerCountTracker implements Copyable<TriggerCountTracker> {
    public static final Codec<TriggerCountTracker> CODEC = Codec.unboundedMap(
        Identifier.CODEC,
        Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(i -> (Object2IntMap<String>)new Object2IntOpenHashMap<>(i), Function.identity())
    ).xmap(TriggerCountTracker::new, tracker -> tracker.entries);

    private final Map<Identifier, Object2IntMap<String>> entries = new HashMap<>();

    public TriggerCountTracker(Map<Identifier, Object2IntMap<String>> entries) {
        this.entries.putAll(entries);
    }

    public int update(AdvancementHolder advancement, String criterionName) {
        return entries.computeIfAbsent(advancement.id(), id -> new Object2IntOpenHashMap<>()).computeInt(criterionName, (key, initial) -> (initial == null ? 0 : initial) + 1);
    }

    public void removeGranted(ServerPlayer player, PlayerAdvancements tracker) {
        entries.entrySet().removeIf(entry -> {
            @Nullable
            AdvancementHolder a = player.level().getServer().getAdvancements().get(entry.getKey());
            return a == null || tracker.getOrStartProgress(a).isDone();
        });
    }

    @Override
    public void copyFrom(TriggerCountTracker other, boolean alive) {
        entries.clear();
        entries.putAll(other.entries);
    }
}
