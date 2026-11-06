package com.minelittlepony.unicopia.mixin.server;

import java.util.stream.LongStream;
import net.minecraft.core.BlockPos;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.ability.magic.spell.effect.PortalSpell;
import com.minelittlepony.unicopia.server.world.Ether;

import it.unimi.dsi.fastutil.longs.LongSet;

@Mixin(PlayerChunkSender.class)
abstract class MixinChunkDataSender {
    @Shadow
    private @Final LongSet pendingChunks;

    @Inject(method = "markChunkPendingToSend", at = @At("RETURN"))
    private void onAdd(LevelChunk chunk, CallbackInfo info) {
        var etherChunk = Ether.get(chunk.getLevel()).getChunk(chunk.getPos());
        if (etherChunk != null) {
            etherChunk.getStates().stream().flatMapToLong(state -> {
                if (state.getSpell() instanceof PortalSpell portal && portal.getDestinationReference().isSet()) {
                    return portal.getDestinationReference()
                        .getTarget()
                        .stream()
                        .mapToLong(target -> ChunkPos.pack(BlockPos.containing(target.pos())));
                }
                return LongStream.empty();
            }).distinct().forEach(pendingChunks::add);
        }
    }
}
