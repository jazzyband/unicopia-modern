package com.minelittlepony.unicopia.server.world;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.KelpPlantBlock;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class WaterLoggingManager<O, S extends StateHolder<O, S>> {
    private static final WaterLoggingManager<?, ?> INSTANCE = new WaterLoggingManager<>();

    @SuppressWarnings("unchecked")
    public static <O, S extends StateHolder<O, S>> WaterLoggingManager<O, S> getInstance() {
        return (WaterLoggingManager<O, S>)INSTANCE;
    }

    private final boolean enabled;

    public WaterLoggingManager() {
        enabled = !Unicopia.getConfig().disableWaterPlantsFix.get();
    }

    public void appendProperties(O owner, StateBuilder builder) {
        if (appliesTo(owner)) {
            builder.addIfNotPresent(BlockStateProperties.WATERLOGGED);
        }
    }

    public S getDefaultState(O owner, S defaultState) {
        if (appliesTo(owner, defaultState)) {
            return defaultState.setValue(BlockStateProperties.WATERLOGGED, !(owner instanceof BedBlock));
        }
        return defaultState;
    }

    @SuppressWarnings("unchecked")
    public BlockState getPlacementState(BlockState state, BlockPlaceContext context) {
        if (state != null && appliesTo((O)state.getBlock(), (S)state)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(FluidTags.WATER));
        }
        return state;
    }

    public void getFluidState(O owner, S state, CallbackInfoReturnable<FluidState> info) {
        if (appliesTo(owner, state)) {
            info.setReturnValue((state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER : Fluids.EMPTY).defaultFluidState());
        }
    }

    public BlockState getUpdatedState(LevelReader world, BlockPos pos, BlockState oldState, BlockState newState) {
        if (shouldPreventRemoval(world, pos, oldState, newState)) {
            return oldState;
        }
        return newState;
    }

    public boolean appliesTo(O block, S state) {
        return appliesTo(block) && state.hasProperty(BlockStateProperties.WATERLOGGED);
    }

    public boolean appliesTo(O block) {
        return enabled
            && (block instanceof SeagrassBlock
                || block instanceof TallSeagrassBlock
                || block instanceof KelpBlock
                || block instanceof KelpPlantBlock
                || block instanceof BedBlock);
    }

    public boolean shouldPreventRemoval(LevelReader world, BlockPos pos, BlockBehaviour.BlockStateBase oldState, BlockBehaviour.BlockStateBase newState) {
        return enabled
                && newState.isAir()
                && oldState.hasProperty(BlockStateProperties.WATERLOGGED)
                && oldState.getBlock() instanceof TallSeagrassBlock
                && oldState.hasProperty(DoublePlantBlock.HALF)
                && oldState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER
                && world.getBlockState(pos.above()).is(oldState.getBlock());
    }

    public interface StateBuilder {
        void addIfNotPresent(Property<?> property);
    }
}
