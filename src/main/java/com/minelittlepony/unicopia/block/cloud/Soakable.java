package com.minelittlepony.unicopia.block.cloud;

import java.util.Arrays;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.mojang.serialization.Codec;

public interface Soakable {
    Codec<Soakable> CODEC = BuiltInRegistries.BLOCK.byNameCodec().xmap(b -> (Soakable)b, s -> (Block)s);

    IntegerProperty MOISTURE = IntegerProperty.create("moisture", 1, 7);
    Direction[] DIRECTIONS = Arrays.stream(Direction.values()).filter(d -> d != Direction.UP).toArray(Direction[]::new);

    @Nullable
    BlockState getStateWithMoisture(BlockState state, int moisture);

    default int getMoisture(BlockState state) {
        return state.getOptionalValue(MOISTURE).orElse(0);
    }

    static void addMoistureParticles(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) {
            world.addParticle(ParticleTypes.DRIPPING_WATER,
                    pos.getX() + random.nextFloat(),
                    pos.getY(),
                    pos.getZ() + random.nextFloat(),
                    0, 0, 0
            );
        }
    }

    static InteractionResult tryCollectMoisture(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getBlock() instanceof Soakable soakable) {
            if (stack.is(Items.GLASS_BOTTLE)) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Items.POTION.getDefaultInstance(), false));
                world.playSound(player, player.getX(), player.getY(), player.getZ(), USounds.Vanilla.BOTTLE_FILL, SoundSource.NEUTRAL, 1, 1);
                world.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
                updateMoisture(soakable, state, world, pos, soakable.getMoisture(state) - 1);

                return InteractionResult.SUCCESS;
            }
        }

        return tryDepositMoisture(stack, state, world, pos, player, hand, hit);
    }

    static InteractionResult tryDepositMoisture(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getBlock() instanceof Soakable soakable) {
            if (soakable.getMoisture(state) < 7
                    && stack.is(Items.POTION)
                    && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Items.GLASS_BOTTLE.getDefaultInstance(), false));
                world.playSound(player, player.getX(), player.getY(), player.getZ(), USounds.Vanilla.BUCKET_EMPTY, SoundSource.NEUTRAL, 1, 1);
                world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
                updateMoisture(soakable, state, world, pos, soakable.getMoisture(state) + 1);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    static void tickMoisture(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getBlock() instanceof Soakable soakable) {
            int moisture = soakable.getMoisture(state);

            if (world.isRainingAt(pos) && world.isEmptyBlock(pos.above())) {
                if (moisture < 7) {
                    world.setBlockAndUpdate(pos, soakable.getStateWithMoisture(state, moisture + 1));
                }
            } else if (!world.isEmptyBlock(pos.above())) {
                if (moisture > 1) {
                    BlockPos neighborPos = pos.relative(Util.getRandom(Soakable.DIRECTIONS, random));
                    BlockState neighborState = world.getBlockState(neighborPos);

                    if (neighborState.getBlock() instanceof Soakable neighborSoakable && neighborSoakable.getMoisture(neighborState) < moisture) {
                        int half = Math.max(1, moisture / 2);
                        @Nullable
                        BlockState newNeighborState = neighborSoakable.getStateWithMoisture(neighborState, half);
                        if (newNeighborState != null) {
                            updateMoisture(soakable, state, world, pos, moisture - half);
                            world.setBlockAndUpdate(neighborPos, soakable.getStateWithMoisture(state, half));
                            world.gameEvent(null, GameEvent.BLOCK_CHANGE, neighborPos);
                            return;
                        }
                    }
                }
                updateMoisture(soakable, state, world, pos, moisture - 1);
            }
        }
    }

    private static void updateMoisture(Soakable soakable, BlockState state, Level world, BlockPos pos, int newMoisture) {
        world.setBlockAndUpdate(pos, soakable.getStateWithMoisture(state, newMoisture));
        world.playSound(null, pos, SoundEvents.SALMON_FLOP, SoundSource.BLOCKS, 1, (float)world.getRandom().triangle(0.5, 0.3F));
    }
}
