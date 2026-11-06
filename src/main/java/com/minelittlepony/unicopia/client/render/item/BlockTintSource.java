package com.minelittlepony.unicopia.client.render.item;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.TintedBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record BlockTintSource() implements ItemTintSource {
    public static final BlockTintSource INSTANCE = new BlockTintSource();
    public static final MapCodec<BlockTintSource> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int calculate(ItemStack stack, ClientLevel world, LivingEntity user) {
        return getColor(Block.byItem(stack.getItem()).defaultBlockState(), null, null, -1);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }

    public static int getColor(BlockState state, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos, int tintIndex) {
        int color = view == null || pos == null ? FoliageColor.FOLIAGE_DEFAULT : BiomeColors.getAverageFoliageColor(view, pos);

        if (state.getBlock() instanceof TintedBlock block) {
            return ARGB.opaque(block.getTint(state, view, pos, color));
        }

        return color;
    }

}
