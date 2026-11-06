package com.minelittlepony.unicopia.compat.seasons;

import com.minelittlepony.unicopia.mixin.seasons.MixinFertilizableUtil;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public interface FertilizableUtil {
    boolean MOD_LOADED = FabricLoader.getInstance().isModLoaded("seasons");

    static float getMultiplier(ServerLevel world, BlockPos pos, BlockState state) {
        if (MOD_LOADED) {
            return MixinFertilizableUtil.getMultiplier(world, pos, state);
        }
        return 0;
    }

    static int getGrowthSteps(ServerLevel world, BlockPos pos, BlockState state, RandomSource random) {
        float multiplier = 1 + FertilizableUtil.getMultiplier(world, pos, state);
        int steps = 0;

        while (multiplier > 0) {
            if (multiplier >= random.nextFloat()) {
                multiplier -= 1;
                steps++;
            }
        }

        return steps;
    }
}
