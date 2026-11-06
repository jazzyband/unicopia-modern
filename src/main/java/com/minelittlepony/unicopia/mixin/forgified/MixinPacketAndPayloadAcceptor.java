package com.minelittlepony.unicopia.mixin.forgified;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "net.neoforged.neoforge.network.bundle.PacketAndPayloadAcceptor")
abstract class MixinPacketAndPayloadAcceptor<L extends ClientCommonPacketListener> implements Consumer<Packet<? super L>> {
    @Shadow(remap = false)
    private @Final Consumer<Packet<? super L>> consumer;

    @Override
    public void accept(Packet<? super L> packet) {
        consumer.accept(packet);
    }
}
