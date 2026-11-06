package com.minelittlepony.unicopia.server.world;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;

public class OfflinePlayerCache {
    private static final LoadingCache<Key, Optional<ServerPlayer>> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build(CacheLoader.from(key -> {
                ServerPlayer offlinePlayer = FakePlayer.get(key.world(), new GameProfile(key.playerId(), "[Offline Player]"));

                var savedData = key.world().getServer().getPlayerList().loadPlayerData(new NameAndId(key.playerId(), "[Offline Player]"));
                if (savedData.isPresent()) {
                    offlinePlayer.load(TagValueInput.create(ProblemReporter.DISCARDING, key.world().registryAccess(), savedData.get()));
                    return Optional.of(offlinePlayer);
                }

                return Optional.empty();
            }));

    @Nullable
    public static ServerPlayer getOfflinePlayer(ServerLevel world, UUID playerId) {
        ServerPlayer player = (ServerPlayer)world.getPlayerByUUID(playerId);
        if (player == null) {
            player = world.getServer().getPlayerList().getPlayer(playerId);
        }
        if (player == null) {
            return CACHE.getUnchecked(new Key(world, playerId)).orElse(null);
        }
        return player;
    }

    record Key (ServerLevel world, UUID playerId) {}
}
