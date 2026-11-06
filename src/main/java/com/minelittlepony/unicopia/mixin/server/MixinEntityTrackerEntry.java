package com.minelittlepony.unicopia.mixin.server;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.network.track.Trackable;

@Mixin(ServerEntity.class)
abstract class MixinEntityTrackerEntry {
    @Shadow
    private @Final Entity entity;
    @Shadow
    private @Final ServerEntity.Synchronizer synchronizer;

    @Inject(method = "sendChanges()V", at = @At("TAIL"))
    private void unicopia_onTick(CallbackInfo info) {
        Trackable.of(entity).getDataTrackers().tick(packet -> synchronizer.sendToTrackingPlayersAndSelf((Packet)packet));
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "sendPairingData", at = @At("RETURN"), require = 0)
    private void unicopia_onSendPackets(ServerPlayer player, @Coerce Object sender, CallbackInfo info) {
        // NEO: Consumer<Packet<ClientPlayPacketListener>> replaced with PacketAndPayloadAcceptor<ClientPlayPacketListener>
        if (sender instanceof Consumer s) {
            Trackable.of(entity).getDataTrackers().sendInitial(player, s);
        }
    }
}
