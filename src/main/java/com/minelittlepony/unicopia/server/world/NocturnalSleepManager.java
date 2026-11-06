package com.minelittlepony.unicopia.server.world;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gamerules.GameRules;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

public class NocturnalSleepManager extends SleepStatus {
    public static final long DAY_LENGTH = 24000L;

    private final ServerLevel world;

    public NocturnalSleepManager(ServerLevel world) {
        this.world = world;
    }

    public String getTimeSkippingMessage(String nightSkippingMessage) {
        if (world.getGameRules().get(UGameRules.DO_NOCTURNAL_BAT_PONIES) && world.isBrightOutside()) {
            return "sleep.skipping_day";
        }

        return nightSkippingMessage;
    }

    public void skipTime() {
        if (world.getGameRules().get(GameRules.ADVANCE_TIME) && world.players().stream()
                .filter(LivingEntity::isSleeping)
                .map(Pony::of)
                .map(Pony::getSpecies)
                .noneMatch(Race::isDayurnal)) {
            world.clockManager().setTotalTicks(
                    world.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD),
                    world.getOverworldClockTime() - DAY_LENGTH + 13500);
        }
    }

    public List<ServerPlayer> getApplicablePlayer() {
        return filterPlayers(world.players());
    }

    public List<ServerPlayer> filterPlayers(List<ServerPlayer> players) {
        if (!world.getGameRules().get(UGameRules.DO_NOCTURNAL_BAT_PONIES)) {
            return players;
        }

        return players.stream().filter(player -> {
            Pony pony = Pony.of(player);
            return (pony.getSpecies().isNocturnal() == world.isBrightOutside());
        }).toList();
    }

    public interface Source {
        NocturnalSleepManager getNocturnalSleepManager();
    }
}
