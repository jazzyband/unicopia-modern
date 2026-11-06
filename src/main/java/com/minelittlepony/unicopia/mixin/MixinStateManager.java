package com.minelittlepony.unicopia.mixin;

import java.util.Map;
import java.util.function.Function;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.server.world.WaterLoggingManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateDefinition.Factory;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

@Mixin(StateDefinition.class)
abstract class MixinStateManager<O, S extends StateHolder<O, S>> {
    @Shadow
    private @Final O owner;

    @ModifyReturnValue(method = "any", at = @At("RETURN"))
    private S onGetDefaultState(S state) {
        return WaterLoggingManager.<O, S>getInstance().getDefaultState(owner, state);
    }
}

@Mixin(StateDefinition.Builder.class)
abstract class MixinStateManagerBuilder<O, S extends StateHolder<O, S>> implements WaterLoggingManager.StateBuilder {
    @Shadow
    private @Final O owner;

    @Shadow
    private @Final Map<String, Property<?>> properties;

    @Inject(method = "create", at = @At("HEAD"))
    private void build(Function<O, S> defaultStateGetter, Factory<O, S> factory, CallbackInfoReturnable<StateDefinition<O, S>> info) {
        WaterLoggingManager.<O, S>getInstance().appendProperties(owner, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addIfNotPresent(Property<?> property) {
        if (!properties.containsValue(property)) {
            ((StateDefinition.Builder<O, S>)(Object)this).add(property);
        }
    }
}

@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class MixinBlockState extends StateHolder<Block, BlockState> {
    MixinBlockState() {super(null, null, null);}

    @Shadow
    protected abstract BlockState asState();

    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
    private void onGetFluidState(CallbackInfoReturnable<FluidState> info) {
        WaterLoggingManager.<Block, BlockState>getInstance().getFluidState(owner, asState(), info);
    }

    @ModifyReturnValue(method = "updateShape", at = @At("RETURN"))
    private BlockState onGetStateForNeighborUpdate(BlockState updatedState, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return WaterLoggingManager.getInstance().getUpdatedState(world, pos, asState(), updatedState);
    }
}
