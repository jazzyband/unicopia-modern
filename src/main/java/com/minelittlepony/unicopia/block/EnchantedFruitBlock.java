package com.minelittlepony.unicopia.block;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;

public class EnchantedFruitBlock extends FruitBlock {
    public static final BooleanProperty ENCHANTED = BooleanProperty.create("enchanted");


    public EnchantedFruitBlock(Direction attachmentFace, Block stem, ResourceKey<Item> fruitKey, VoxelShape shape, boolean flammable, Properties settings) {
        this(attachmentFace, Set.of(stem), fruitKey, shape, flammable, settings);
    }

    public EnchantedFruitBlock(Direction attachmentFace, Set<Block> stem, ResourceKey<Item> fruitKey, VoxelShape shape, boolean flammable, Properties settings) {
        super(attachmentFace, stem, fruitKey, shape, flammable, settings);
        registerDefaultState(defaultBlockState().setValue(ENCHANTED, false));
    }


    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return (state.getValue(ENCHANTED) ? Items.ENCHANTED_GOLDEN_APPLE : Items.GOLDEN_APPLE).getDefaultInstance();
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);
        if (state.getValue(ENCHANTED)) {
            ParticleUtils.spawnParticleBelow(world, pos, random, ParticleTypes.ELECTRIC_SPARK);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ENCHANTED);
    }
}
