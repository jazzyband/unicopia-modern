package com.minelittlepony.unicopia.mixin.seasons;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(
        targets = { "io.github.lucaargolo.seasons.utils.FertilizableUtil" },
        remap = false
)
public interface MixinFertilizableUtil {
    @Invoker("getMultiplier")
    static float getMultiplier(ServerLevel world, BlockPos pos, BlockState state) {
        return 0;
    }
}
