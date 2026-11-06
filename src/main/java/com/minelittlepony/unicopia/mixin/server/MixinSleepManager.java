package com.minelittlepony.unicopia.mixin.server;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.server.world.NocturnalSleepManager;

@Mixin(SleepStatus.class)
abstract class MixinSleepManager {
    @ModifyVariable(method = "update(Ljava/util/List;)Z", at = @At("HEAD"))
    public List<ServerPlayer> modifyPlayers(List<ServerPlayer> players) {
        return players.size() <= 0 ? players : ((NocturnalSleepManager.Source)players.get(0).level()).getNocturnalSleepManager().filterPlayers(players);
    }
}
