package com.minelittlepony.unicopia.mixin.server;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(PlayerList.class)
abstract class MixinPlayerManager {
    @Inject(method = "remove", at = @At("HEAD"))
    public void onRemove(ServerPlayer player, CallbackInfo info) {
        player.getPassengers().stream()
            .flatMap(passenger -> Pony.of(passenger).stream())
            .forEach(passenger -> passenger.setCarrier((Entity)null));
        player.ejectPassengers();

        @Nullable
        Entity vehicle = null;
        if (player.isPassenger() && (vehicle = player.getRootVehicle()).hasExactlyOnePlayerPassenger()) {
            if (vehicle.getPassengersAndSelf().anyMatch(e -> {
                return e != player && e instanceof Player;
            })) {
                player.stopRiding();
                Pony.of(player).setCarrier((Entity)null);
            }
        }
    }
}
