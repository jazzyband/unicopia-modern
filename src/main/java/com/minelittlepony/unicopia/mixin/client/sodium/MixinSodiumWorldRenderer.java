package com.minelittlepony.unicopia.mixin.client.sodium;

import java.util.SortedSet;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

@Pseudo
@Mixin(
        targets = { "me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer" },
        remap = false
)
abstract class MixinSodiumWorldRenderer {
    @ModifyVariable(
            method = { "renderTileEntities", "renderBlockEntities" },
            at = @At("HEAD"),
            require = 0
    )
    public Long2ObjectMap<SortedSet<BlockDestructionProgress>> modifyDestruction(Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions) {
        ClientBlockDestructionManager destructions = ((ClientBlockDestructionManager.Source)Minecraft.getInstance().levelRenderer).getDestructionManager();
        return destructions.getCombinedDestructions(blockBreakingProgressions);
    }
}
