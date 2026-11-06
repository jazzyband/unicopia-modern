package com.minelittlepony.unicopia.block.cloud;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.container.ShapingBenchScreenHandler;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShapingBenchBlock extends CloudBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 13, 0, 3, 18, 3),
            Block.box(13, 13, 0, 16, 18, 3),
            Block.box(0, 13, 13, 3, 18, 16),
            Block.box(13, 13, 13, 16, 18, 16),
            Block.box(0, 13, 0, 16, 16, 16),
            Block.box(2, 0, 2, 14, 17, 14),
            Block.box(0, 0, 0, 16, 4, 16)
    );

    public ShapingBenchBlock(Properties settings) {
        super(false, settings);
    }


    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, EquineContext equineContext) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(state.getMenuProvider(world, pos));
        player.awardStat(Stats.INTERACT_WITH_STONECUTTER);
        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return new SimpleMenuProvider((syncId, playerInventory, player) -> {
            return new ShapingBenchScreenHandler(syncId, playerInventory, ContainerLevelAccess.create(world, pos));
        }, getName());
    }
}
