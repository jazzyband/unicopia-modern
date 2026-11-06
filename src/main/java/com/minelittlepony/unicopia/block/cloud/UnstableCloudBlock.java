package com.minelittlepony.unicopia.block.cloud;

import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.mojang.serialization.MapCodec;
import com.minelittlepony.unicopia.util.PosHelper;

public class UnstableCloudBlock extends CloudBlock {
    private static final int MAX_CHARGE = 6;
    private static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, MAX_CHARGE);

    public UnstableCloudBlock(Properties settings) {
        super(false, settings);
        registerDefaultState(defaultBlockState().setValue(CHARGE, 0));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);
        int charge = state.getValue(CHARGE);
        if (charge > 0) {
            world.addParticle(new LightningBoltParticleEffect(true, 10, 1, 0.6F + (charge / (float)MAX_CHARGE) * 0.4F, Optional.empty()), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
        }
    }

    @Override
    public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        super.fallOn(world, state, pos, entity, fallDistance);

        for (int i = 0; i < 9; i++) {
            world.addParticle(ParticleTypes.CLOUD, pos.getX() + world.getRandom().nextFloat(), pos.getY() + world.getRandom().nextFloat(), pos.getZ() + world.getRandom().nextFloat(), 0, 0, 0);
        }
        world.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 1, 1);

        if (world.isClientSide()) {
            return;
        }

        if (fallDistance > 3) {
            world.destroyBlock(pos, true);
            return;
        }

        if (state.getValue(CHARGE) < MAX_CHARGE) {
            world.setBlockAndUpdate(pos, state.cycle(CHARGE));
        } else {
            world.setBlockAndUpdate(pos, state.setValue(CHARGE, 0));


            if (world instanceof ServerLevel sw) {
                Optional<BlockPos> lightningRodPos = sw.getPoiManager().findClosest(
                        poiType -> poiType.is(PoiTypes.LIGHTNING_ROD),
                        innerPos -> innerPos.getY() == world.getHeight(Heightmap.Types.WORLD_SURFACE, innerPos.getX(), innerPos.getZ()) - 1,
                        pos, 10, PoiManager.Occupancy.ANY
                );
                BlockPos shockPosition = lightningRodPos.or(() -> {
                    return sw.getEntities(entity, new AABB(pos.below()).inflate(5, 0, 5).expandTowards(0, -10, 0)).stream().findAny().map(Entity::blockPosition);
                }).orElseGet(() -> PosHelper.findNearestSurface(sw, pos.offset(world.getRandom().nextInt(10) - 5, -world.getRandom().nextInt(10), world.getRandom().nextInt(10) - 5)).immutable());

                ParticleUtils.spawnParticle(world,
                        new LightningBoltParticleEffect(false, 10, 6, 0.3F, Optional.of(Vec3.atCenterOf(shockPosition))),
                        pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                        0, 0, 0
                );
                world.getEntities(null, new AABB(shockPosition).inflate(2)).forEach(e -> {
                    e.hurtServer(sw, entity.damageSources().lightningBolt(), 1);
                });
                lightningRodPos.ifPresent(rodPos -> {
                    BlockState rod = sw.getBlockState(rodPos);
                    if (rod.getBlock() instanceof LightningRodBlock lightningRod) {
                        lightningRod.onLightningStrike(rod, sw, rodPos);
                    }
                });
                LightningBolt.clearCopperOnLightningStrike(world, shockPosition);
                entity.gameEvent(GameEvent.LIGHTNING_STRIKE);

                if (world.isEmptyBlock(shockPosition) && world.getBlockState(shockPosition.below()).ignitedByLava()) {
                    world.setBlockAndUpdate(shockPosition, Blocks.FIRE.defaultBlockState());
                }
            }
        }
    }
}
