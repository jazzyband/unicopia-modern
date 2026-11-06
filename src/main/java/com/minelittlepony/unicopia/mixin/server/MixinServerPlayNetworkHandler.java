package com.minelittlepony.unicopia.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.duck.ServerPlayerEntityDuck;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class MixinServerPlayNetworkHandler implements ServerGamePacketListener {
    @Shadow public ServerPlayer player;
    @Shadow private boolean clientIsFloating;
    @Shadow private int aboveGroundTickCount;

    private boolean prevMotionChecks;

    @Inject(method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V", at = @At("HEAD"))
    private void beforePlayerMove(ServerboundMovePlayerPacket packet, CallbackInfo info) {
        PacketUtils.ensureRunningOnSameThread(packet, this, player.level());
        prevMotionChecks = player.isChangingDimension();
        if (Pony.of(player).getPhysics().isFlyingSurvival) {
            ((ServerPlayerEntityDuck)player).setPreventMotionChecks(true);
        }
    }

    @Inject(method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V", at = @At("RETURN"))
    private void afterPlayerMove(ServerboundMovePlayerPacket packet, CallbackInfo info) {
        ((ServerPlayerEntityDuck)player).setPreventMotionChecks(prevMotionChecks);
        if (Pony.of(player).getPhysics().isFlyingSurvival) {
            clientIsFloating = false;
            aboveGroundTickCount = 0;
            player.fallDistance = 0;
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void beforePlayerTick(CallbackInfo info) {
        if (Pony.of(player).getPhysics().isFlyingSurvival) {
            clientIsFloating = false;
            aboveGroundTickCount = 0;
            player.fallDistance = 0;
        }
    }
}
