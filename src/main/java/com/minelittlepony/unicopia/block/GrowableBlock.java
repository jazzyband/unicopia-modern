package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SpreadingSnowyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class GrowableBlock extends SpreadingSnowyBlock {

    private final Supplier<Block> dead;

    protected GrowableBlock(Supplier<Block> converted, Properties settings) {
        super(settings, Blocks.DIRT.builtInRegistryHolder().key());
        this.dead = converted;
    }


    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);
        if (random.nextInt(2) == 0) {
            world.addParticle(ParticleTypes.MYCELIUM, pos.getX() + random.nextDouble(), pos.getY() + 1.1, pos.getZ() + random.nextDouble(), 0, 0, 0);
        }
        if (random.nextInt(1500) == 0) {
            world.playLocalSound(pos, USounds.BLOCK_CHITIN_AMBIENCE, SoundSource.BLOCKS, 0.13F, 0.2F, true);

            for (int i = 0; i < 9; i++) {
                world.addParticle(random.nextInt(2) == 0 ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.CRIMSON_SPORE,
                        pos.getX() + random.nextDouble(),
                        pos.getY() + 1.1, pos.getZ() + random.nextDouble(),
                        random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5
                );
            }
        }

        if (random.nextInt(20) == 0) {
            for (int i = 0; i < 9; i++) {
                world.addParticle(ParticleTypes.ASH,
                        pos.getX() + random.nextDouble(),
                        pos.getY() + 1.1, pos.getZ() + random.nextDouble(),
                        random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5
                );
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!canBeGrass(state, world, pos)) {
            world.setBlockAndUpdate(pos, dead.get().defaultBlockState());
            return;
        }

        if (world.getMaxLocalRawBrightness(pos.above()) >= 9) {
            BlockState blockState = defaultBlockState();
            for (int i = 0; i < 4; i++) {
                BlockPos blockPos = pos.offset(
                        random.nextInt(3) - 1,
                        random.nextInt(5) - 3,
                        random.nextInt(3) - 1
                );

                if (canPropagate(blockState, world, blockPos)) {
                    world.setBlockAndUpdate(blockPos, blockState.setValue(SNOWY, world.getBlockState(blockPos.above()).is(Blocks.SNOW)));
                }
            }
        }
    }

    private boolean canPropagate(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos).is(dead.get())
                && !world.getFluidState(pos.above()).is(FluidTags.WATER)
                && canBeGrass(state, world, pos);
    }

    private boolean canBeGrass(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState stateAbove = world.getBlockState(above);
        if (stateAbove.is(Blocks.SNOW) && stateAbove.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        if (stateAbove.getFluidState().getAmount() == 8) {
            return false;
        }

        return stateAbove.getLightDampening() < 15;
    }
}
