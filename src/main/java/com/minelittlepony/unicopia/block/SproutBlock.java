package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SproutBlock extends CropBlock implements TintedBlock {

    private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[]{
            Block.box(7, 0, 7, 9, 2, 9),
            Block.box(7, 0, 7, 9, 4, 9),
            Block.box(7, 0, 7, 9, 6, 9),
            Block.box(7, 0, 7, 9, 8, 9),
            Block.box(7, 0, 7, 9, 10, 9),
            Block.box(7, 0, 7, 9, 12, 9),
            Block.box(7, 0, 7, 9, 14, 9),
            Block.box(7, 0, 7, 9, 16, 9)
    };

    public static Properties settings() {
        return Properties.of()
                .noCollision()
                .randomTicks()
                .instabreak()
                .sound(SoundType.HARD_CROP)
                .pushReaction(PushReaction.POPPED);
    }

    private final ItemLike seeds;

    private final Supplier<BlockState> matureState;

    private final int overlay;

    public SproutBlock(int overlay, ItemLike seeds, Supplier<BlockState> matureState, Properties settings) {
        super(settings);
        this.seeds = seeds;
        this.matureState = matureState;
        this.overlay = overlay;
    }


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return AGE_TO_SHAPE[state.getValue(getAgeProperty())];
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.randomTick(state, world, pos, random);
        state = world.getBlockState(pos);
        if (state.is(this)) {
            onGrow(world, state, pos);
        }
    }

    @Override
    public void growCrops(Level world, BlockPos pos, BlockState state) {
        super.growCrops(world, pos, state);
        state = world.getBlockState(pos);
        if (state.is(this)) {
            onGrow(world, world.getBlockState(pos), pos);
        }
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seeds;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return seeds.asItem().getDefaultInstance();
    }

    protected void onGrow(Level world, BlockState state, BlockPos pos) {
        if (isMaxAge(state)) {
            mature(world, state, pos);
        }
    }

    protected void mature(Level world, BlockState state, BlockPos pos) {
        state = matureState.get();
        world.setBlockAndUpdate(pos, state);
        SoundType group = state.getSoundType();
        world.playSound(null, pos, group.getPlaceSound(), SoundSource.BLOCKS, group.getVolume(), group.getPitch());
    }

    @Override
    public int getTint(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int foliageColor) {
        return TintedBlock.blend(foliageColor, overlay);
    }
}
