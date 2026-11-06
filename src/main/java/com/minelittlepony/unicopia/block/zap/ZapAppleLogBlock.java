package com.minelittlepony.unicopia.block.zap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class ZapAppleLogBlock extends RotatedPillarBlock implements ElectrifiedBlock {
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

    public static Properties settings(MapColor topMapColor, MapColor sideMapColor) {
        return Properties.of()
            .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0f)
            .sound(SoundType.WOOD)
            .ignitedByLava();
    }

    private final BlockState artifialModelBlock;

    public ZapAppleLogBlock(BlockState artifialModelBlock, Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(NATURAL, true));
        this.artifialModelBlock = artifialModelBlock;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NATURAL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(NATURAL, false);
    }

    @Deprecated
    @Override
    public void attack(BlockState state, Level world, BlockPos pos, Player player) {
        triggerLightning(state, world, pos);
    }

    @Deprecated
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        if (!state.getValue(NATURAL)) {
            return artifialModelBlock.getDestroyProgress(player, world, pos);
        }

        return getBlockBreakingDelta(super.getDestroyProgress(state, player, world, pos), player);
    }
}
