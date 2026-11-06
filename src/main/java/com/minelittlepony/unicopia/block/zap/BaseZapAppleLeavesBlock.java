package com.minelittlepony.unicopia.block.zap;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.BlockConstructionUtils;
import com.minelittlepony.unicopia.block.TintedBlock;
import com.minelittlepony.unicopia.block.UMapColors;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BaseZapAppleLeavesBlock extends TintedParticleLeavesBlock implements TintedBlock, ZapStagedBlock, ElectrifiedBlock {

    public static Properties settings() {
        return Properties.of()
                .mapColor(UMapColors.ZAP_LEAVES)
                .strength(500, 1200)
                .randomTicks()
                .sound(SoundType.AZALEA_LEAVES)
                .noOcclusion()
                .isValidSpawn(BlockConstructionUtils::canSpawnOnLeaves)
                .isSuffocating(BlockConstructionUtils::never)
                .isViewBlocking(BlockConstructionUtils::never);
    }

    public BaseZapAppleLeavesBlock(Properties settings) {
        super(0.01F, settings);
    }


    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.getValue(PERSISTENT) || oldState.is(state.getBlock())) {
            return;
        }
        updateStage(state, world, pos);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(state, world, pos, random);
        if (state.getValue(PERSISTENT)) {
            return;
        }
        tryAdvanceStage(state, world, pos, random);
    }

    @Override
    public ZapAppleStageStore.Stage getStage(BlockState state) {
        return ZapAppleStageStore.Stage.FLOWERING;
    }

    @Deprecated
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        if (state.getValue(PERSISTENT)) {
            return Blocks.OAK_LEAVES.defaultBlockState().getDestroyProgress(player, world, pos);
        }

        float delta = super.getDestroyProgress(state, player, world, pos);

        if (Pony.of(player).getCompositeRace().canUseEarth()) {
            delta *= 50;
        }

        if (getStage(state) == ZapAppleStageStore.Stage.RIPE) {
            delta *= 5;
        }

        return Mth.clamp(delta, 0, 0.9F);
    }

    @Override
    public void attack(BlockState state, Level world, BlockPos pos, Player player) {
        triggerLightning(state, world, pos);
    }

    @Override
    public int getTint(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int foliageColor) {
        if (pos == null) {
            return 0x4C7EFA;
        }

        return TintedBlock.blend(TintedBlock.rotate(foliageColor, 2), 0x0000FF, 0.3F);
    }
}
