package com.minelittlepony.unicopia.block.zap;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ZapAppleLeavesBlock extends BaseZapAppleLeavesBlock {
    public static final EnumProperty<ZapAppleStageStore.Stage> STAGE = EnumProperty.create("stage", ZapAppleStageStore.Stage.class);

    public ZapAppleLeavesBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(STAGE, ZapAppleStageStore.Stage.GREENING));
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(STAGE, ZapAppleStageStore.Stage.GREENING);
    }

    @Override
    public ZapAppleStageStore.Stage getStage(BlockState state) {
        return state.getValue(STAGE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STAGE);
    }
}
