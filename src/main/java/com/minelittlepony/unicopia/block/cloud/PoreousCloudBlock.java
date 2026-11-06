package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.state.StateUtil;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PoreousCloudBlock extends CloudBlock implements Soakable {

    @Nullable
    protected final Supplier<Soakable> soggyBlock;

    public PoreousCloudBlock(boolean meltable, @Nullable Supplier<Soakable> soggyBlock, Properties settings) {
        super(meltable, settings.noOcclusion());
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
        return soggyBlock == null ? null : soggyBlock.get().getStateWithMoisture(state, moisture);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getBlock() instanceof Soakable soakable && world.isRainingAt(pos) && world.isEmptyBlock(pos.above())) {
            @Nullable
            BlockState soggyState = soakable.getStateWithMoisture(state, random.nextIntBetweenInclusive(1, 5));
            if (soggyState != null) {
                world.setBlockAndUpdate(pos, soggyState);
                return;
            }
        }

        super.randomTick(state, world, pos, random);
    }
}
