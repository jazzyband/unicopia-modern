package com.minelittlepony.unicopia.block.zap;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ElectrifiedFenceBlock extends FenceBlock implements ElectrifiedBlock {

    public ElectrifiedFenceBlock(Properties settings) {
        super(settings);
    }


    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);
        spawnElectricalParticles(world, pos, random);
    }

    @Deprecated
    @Override
    public void attack(BlockState state, Level world, BlockPos pos, Player player) {
        triggerLightning(state, world, pos);
    }

    @Deprecated
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        return getBlockBreakingDelta(super.getDestroyProgress(state, player, world, pos), player);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean isMoving) {
        if (entity instanceof LivingEntity l && l.hurtTime == 0 && l.canBeSeenAsEnemy()) {
            triggerLightning(state, world, pos, l, true);
        }
    }
}
