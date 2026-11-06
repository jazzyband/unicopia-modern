package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.server.world.LightSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Mixin(BlockLightEngine.class)
abstract class MixinChunkBlockLightProvider extends LightEngine {
    @Shadow
    private @Final BlockPos.MutableBlockPos mutablePos;

    MixinChunkBlockLightProvider() { super(null, null); }

    @Inject(method = "propagateLightSources", at = @At("TAIL"))
    private void onPropagateLight(ChunkPos chunkPos, CallbackInfo info) {
        if (chunkSource.getChunkForLighting(chunkPos.x(), chunkPos.z()) instanceof LevelChunk chunk && chunk.getLevel() instanceof ServerLevel world) {
            LightSources.get(world).forEachLightSource(chunkPos, (pos, level) -> {
                enqueueIncrease(pos, LightEngine.QueueEntry.increaseLightFromEmission(level, false));
            });
        }
    }

    @Inject(method = "checkNode", at = @At("TAIL"))
    private void onMethod_51529(long blockPos, CallbackInfo info) {
        long sectionPos = SectionPos.blockToSection(blockPos);
        if (!((MutableBlockLightStorage)storage).invokeHasSection(sectionPos)) {
            return;
        }

        int x = SectionPos.blockToSectionCoord(BlockPos.getX(blockPos));
        int z = SectionPos.blockToSectionCoord(BlockPos.getZ(blockPos));
        if (chunkSource.getChunkForLighting(x, z) instanceof LevelChunk chunk) {
            int lightSourceLight = LightSources.get(chunk.getLevel()).getLuminance(blockPos);
            if (lightSourceLight > 0) {
                enqueueIncrease(blockPos, LightEngine.QueueEntry.increaseLightFromEmission(lightSourceLight, false));
            }
        }
    }

    @Inject(method = "propagateDecrease", at = @At("TAIL"))
    private void onMethod_51530(long blockPos, long flags, CallbackInfo info) {
        int x = SectionPos.blockToSectionCoord(BlockPos.getX(blockPos));
        int z = SectionPos.blockToSectionCoord(BlockPos.getZ(blockPos));
        if (chunkSource.getChunkForLighting(x, z) instanceof LevelChunk chunk) {
            int lightLevel = LightEngine.QueueEntry.getFromLevel(flags);
            for (Direction direction : PROPAGATION_DIRECTIONS) {
                int j;
                long m;
                if (!LightEngine.QueueEntry.shouldPropagateInDirection(flags, direction)
                        || !((MutableBlockLightStorage)storage).invokeHasSection(SectionPos.blockToSection(m = BlockPos.offset(blockPos, direction)))
                        || (j = ((MutableBlockLightStorage)storage).invokeGet(m)) == 0) continue;
                if (j <= lightLevel - 1) {
                    int lightSourceLight = LightSources.get(chunk.getLevel()).getLuminance(blockPos);
                    if (lightSourceLight <= 0) continue;
                    enqueueIncrease(m, LightEngine.QueueEntry.increaseLightFromEmission(lightSourceLight, false));
                    continue;
                }
                enqueueIncrease(m, LightEngine.QueueEntry.increaseOnlyOneDirection(j, false, direction.getOpposite()));
            }
        }
    }
}

@Mixin(LayerLightSectionStorage.class)
interface MutableBlockLightStorage {
    @Invoker("storingLightForSection")
    boolean invokeHasSection(long sectionPos);
    @Invoker("getStoredLevel")
    int invokeGet(long blockPos);
    @Invoker("setStoredLevel")
    void invokeSet(long blockPos, int light);
}
