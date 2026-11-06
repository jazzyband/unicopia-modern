package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class MixinServerPlayNetworkHandler implements ServerGamePacketListener, ServerPlayerConnection, TickablePacketListener {
    @Shadow public ServerPlayer player;

    @ModifyArg(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;handlePlayerPositionChange(DDDFFZZ)V"), index = 5)
    private boolean flipLandingFlag(boolean value) {
        if (Pony.of(this.player).getPhysics().isGravityNegative()) {
            return !value;
        }
        return value;
    }
}
