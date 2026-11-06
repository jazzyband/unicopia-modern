package com.minelittlepony.unicopia.item.cloud;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.google.common.base.Suppliers;

public class CloudBedItem extends CloudBlockItem implements Supplier<BlockEntity> {

    private final Supplier<BlockEntity> renderEntity;

    public CloudBedItem(Block block, Properties settings) {
        super(block, settings);
        this.renderEntity = Suppliers.memoize(() -> ((EntityBlock)block).newBlockEntity(BlockPos.ZERO, block.defaultBlockState()));
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        return context.getLevel().setBlock(context.getClickedPos(), state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE | Block.UPDATE_KNOWN_SHAPE);
    }

    @Override
    public BlockEntity get() {
        return renderEntity.get();
    }
}
