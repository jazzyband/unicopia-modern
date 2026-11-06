package com.minelittlepony.unicopia.block.zap;

import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface ZapStagedBlock {
    ZapAppleStageStore.Stage getStage(BlockState state);

    default void updateStage(BlockState state, Level world, BlockPos pos) {
        if (!(world instanceof ServerLevel sw)) {
            return;
        }
        ZapAppleStageStore.Stage currentStage = ZapAppleStageStore.get(sw).getStage();
        if (currentStage != getStage(state)) {
            state = getState(currentStage);
            changeLeavesState(currentStage, world, state, pos);
        }
        world.scheduleTick(pos, state.getBlock(), 1);
    }

    default void tryAdvanceStage(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        ZapAppleStageStore store = ZapAppleStageStore.get(world);
        ZapAppleStageStore.Stage currentStage = store.getStage();
        if (!world.isBrightOutside() && currentStage != getStage(state)) {
            int transitionRate = getTransitionRate(currentStage);
            if (transitionRate == 0 || random.nextInt(transitionRate) == 0) {
                state = getState(currentStage);
                changeLeavesState(currentStage, world, state, pos);
                onStageChanged(store, currentStage, world, state, pos, random);
            }
        }
        world.scheduleTick(pos, state.getBlock(), 1);
    }

    default int getTransitionRate(ZapAppleStageStore.Stage stage) {
        if (stage == ZapAppleStageStore.Stage.HIBERNATING || stage == ZapAppleStageStore.Stage.GREENING) {
            return 10;
        }
        if (stage == ZapAppleStageStore.Stage.RIPE) {
            return 300;
        }
        return 1500;
    }

    default BlockState getState(ZapAppleStageStore.Stage stage) {
        if (stage == ZapAppleStageStore.Stage.HIBERNATING) {
            return UBlocks.ZAP_LEAVES_PLACEHOLDER.defaultBlockState();
        }
        if (stage == ZapAppleStageStore.Stage.FLOWERING) {
            return UBlocks.FLOWERING_ZAP_LEAVES.defaultBlockState();
        }
        return UBlocks.ZAP_LEAVES.defaultBlockState().setValue(ZapAppleLeavesBlock.STAGE, stage);
    }

    private static void onStageChanged(ZapAppleStageStore store, ZapAppleStageStore.Stage stage, ServerLevel world, BlockState state, BlockPos pos, RandomSource random) {
        BlockPos down = pos.below();
        BlockState below = world.getBlockState(pos.below());

        if (stage == ZapAppleStageStore.Stage.FRUITING && world.isEmptyBlock(down)) {
            if (RandomSource.create(state.getSeed(pos)).nextInt(5) < 2) {
                world.setBlock(down, UBlocks.ZAP_BULB.defaultBlockState(), Block.UPDATE_ALL);
                store.triggerLightningStrike(world, pos);
            }
        }

        if (stage != ZapAppleStageStore.Stage.HIBERNATING && world.getRandom().nextInt(10) == 0) {
            store.triggerLightningStrike(world, pos);
        }

        if (stage == ZapAppleStageStore.Stage.RIPE && below.is(UBlocks.ZAP_BULB)) {
            world.setBlock(down, UBlocks.ZAP_APPLE.defaultBlockState(), Block.UPDATE_ALL);
            store.playMoonEffect(world, pos);
        }

        if (stage == ZapAppleStageStore.Stage.HIBERNATING && (below.is(UBlocks.ZAP_APPLE) || below.is(UBlocks.ZAP_BULB))) {
            world.destroyBlock(down, false);
        }
    }

    private static void changeLeavesState(ZapAppleStageStore.Stage stage, Level world, BlockState state, BlockPos pos) {
        BlockPos down = pos.below();
        BlockState below = world.getBlockState(down);
        if (stage == ZapAppleStageStore.Stage.HIBERNATING && (below.is(UBlocks.ZAP_APPLE) || below.is(UBlocks.ZAP_BULB))) {
            world.destroyBlock(down, false);
        }
        world.setBlockAndUpdate(pos, state);
    }
}
