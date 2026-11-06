package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.state.StateUtil;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PoreousCloudStairsBlock extends CloudStairsBlock implements Soakable {

    protected final Supplier<Soakable> soggyBlock;

    public PoreousCloudStairsBlock(BlockState baseState, Supplier<Soakable> soggyBlock, Properties settings) {
        super(baseState, settings);
        this.soggyBlock = soggyBlock;
    }


    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return Soakable.tryDepositMoisture(stack, state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockState getStateWithMoisture(BlockState state, int moisture) {
        if (moisture <= 0) {
            return StateUtil.copyState(state, defaultBlockState());
        }
        return soggyBlock.get().getStateWithMoisture(state, moisture);
    }
}
