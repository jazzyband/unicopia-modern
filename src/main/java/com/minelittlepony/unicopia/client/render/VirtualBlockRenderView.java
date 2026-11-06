package com.minelittlepony.unicopia.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public interface VirtualBlockRenderView extends BlockAndTintGetter {

    BlockAndTintGetter proxy();

    @Override
    default CardinalLighting cardinalLighting() {
        return proxy().cardinalLighting();
    }

    @Override
    default BlockEntity getBlockEntity(BlockPos pos) {
        return proxy().getBlockEntity(pos);
    }

    @Override
    default BlockState getBlockState(BlockPos pos) {
        return proxy().getBlockState(pos);
    }

    @Override
    default FluidState getFluidState(BlockPos pos) {
        return proxy().getFluidState(pos);
    }

    @Override
    default int getHeight() {
        return proxy().getHeight();
    }

    @Override
    default int getMinY() {
        return proxy().getMinY();
    }

    @Override
    default LevelLightEngine getLightEngine() {
        return proxy().getLightEngine();
    }

    @Override
    default int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
        return proxy().getBlockTint(pos, colorResolver);
    }
}
