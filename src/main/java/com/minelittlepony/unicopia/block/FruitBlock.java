package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.EarthPonyKickAbility.Buckable;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.datafixers.util.Function6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FruitBlock extends Block implements Buckable {
    public static final int DEFAULT_FRUIT_SIZE = 5;
    public static final double DEFAULT_STEM_OFFSET = 2.6F;
    public static final VoxelShape DEFAULT_SHAPE = createFruitShape(DEFAULT_STEM_OFFSET, DEFAULT_FRUIT_SIZE);

    protected final Direction attachmentFace;
    protected final Set<Block> stem;
    protected final VoxelShape shape;
    protected final ResourceKey<Item> fruitKey;

    @Nullable
    private Item cachedItem;

    public static VoxelShape createFruitShape(double stemOffset, double fruitSize) {
        final double min = (16 - fruitSize) * 0.5;
        final double max = 16 - min;
        final double top = 16 - stemOffset;
        return box(min, top - fruitSize, min, max, top, max);
    }


    public FruitBlock(Direction attachmentFace, Block stem, ResourceKey<Item> fruitKey, VoxelShape shape, Properties settings) {
        this(attachmentFace, Set.of(stem), fruitKey, shape, true, settings.sound(SoundType.WOOD).pushReaction(PushReaction.POPPED));
    }

    public FruitBlock(Direction attachmentFace, Block stem, ResourceKey<Item> fruitKey, VoxelShape shape, boolean flammable, Properties settings) {
        this(attachmentFace, Set.of(stem), fruitKey, shape, flammable, settings);
    }

    public FruitBlock(Direction attachmentFace, Set<Block> stem, ResourceKey<Item> fruitKey, VoxelShape shape, Properties settings) {
        this(attachmentFace, stem, fruitKey, shape, true, settings.sound(SoundType.WOOD).pushReaction(PushReaction.POPPED));
    }

    public FruitBlock(Direction attachmentFace, Set<Block> stem, ResourceKey<Item> fruitKey, VoxelShape shape, boolean flammable, Properties settings) {
        super(settings.noOcclusion().isSuffocating(BlockConstructionUtils::never).isViewBlocking(BlockConstructionUtils::never));
        this.attachmentFace = attachmentFace;
        this.stem = stem;
        this.fruitKey = fruitKey;
        this.shape = shape;

        if (flammable) {
            FlammableBlockRegistry.getDefaultInstance().add(this, 20, 50);
        }
    }

    @Override
    public Item asItem() {
        if (cachedItem == null) {
            cachedItem = BuiltInRegistries.ITEM.getValue(fruitKey);
        }

        return cachedItem;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos attachedPos = pos.relative(attachmentFace.getOpposite());
        BlockState attachedState = world.getBlockState(attachedPos);
        return canAttachTo(attachedState);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(world, pos)) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
        if (!state.is(world.getBlockState(pos).getBlock())) {
            BlockState leaves = world.getBlockState(pos.above());
            if (leaves.hasProperty(FruitBearingBlock.STAGE)) {
                world.setBlockAndUpdate(pos.above(), leaves.trySetValue(FruitBearingBlock.AGE, 0).setValue(FruitBearingBlock.STAGE, FruitBearingBlock.Stage.IDLE));
            }
        }
    }

    protected boolean canAttachTo(BlockState state) {
        return stem.contains(state.getBlock());
    }

    @Override
    public List<ItemStack> onBucked(ServerLevel world, BlockState state, BlockPos pos) {
        List<ItemStack> stacks = Block.getDrops(state, world, pos, null);
        world.destroyBlock(pos, false);
        return stacks;
    }
}
