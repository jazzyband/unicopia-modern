package com.minelittlepony.unicopia.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.track.Trackable;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;

@Mixin(ClientPacketListener.class)
abstract class MixinClientPlayNetworkHandler extends ClientCommonPacketListenerImpl {
    protected MixinClientPlayNetworkHandler() { super(null, null, null); }

    @Shadow private ClientLevel level;

    @Nullable
    private LocalPlayer oldPlayer;

    @Inject(method = "handleEntityEvent", at = @At("TAIL"))
    private void onOnEntityStatus(ClientboundEntityEventPacket packet, CallbackInfo info) {
        Living<?> living = Living.living(packet.getEntity(level));
        if (living != null) {
            living.getSpellSlot()
                .get(SpellPredicate.IS_DISGUISE)
                .map(Disguise::getAppearance)
                .map(EntityAppearance::getEntity)
                .ifPresent(appearance -> {
                    appearance.handleEntityEvent(packet.getEventId());
                });
        }
    }

    @Inject(method = "handleRespawn", at = @At("HEAD"))
    public void beforeOnPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo info) {
        oldPlayer = minecraft.player;
    }

    @Inject(method = "handleRespawn", at = @At("RETURN"))
    public void afterOnPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo info) {
        if (oldPlayer != null && oldPlayer != minecraft.player) {
            Trackable.of(oldPlayer).getDataTrackers().copyTo(Trackable.of(minecraft.player).getDataTrackers());
            Pony.of(minecraft.player).copyFrom(Pony.of(oldPlayer), true);
        }
    }
}
