package com.minelittlepony.unicopia.item;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.block.FancyBedBlock;

public class FancyBedItem extends BlockItem implements Supplier<BlockEntity> {

    private final Supplier<BlockEntity> renderEntity;

    public FancyBedItem(Block block, Properties settings) {
        super(block, settings);
        this.renderEntity = Suppliers.memoize(() -> ((EntityBlock)block).newBlockEntity(BlockPos.ZERO, block.defaultBlockState()));
    }

    @Override
    public BlockEntity get() {
        return renderEntity.get();
    }

    @SuppressWarnings("deprecation")
    public static FancyBedBlock.SheetPattern getPattern(ItemStack stack) {
        @Nullable
        TypedEntityData<?> blockEntityNbt = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityNbt == null) {
            return FancyBedBlock.SheetPattern.NONE;
        }
        return blockEntityNbt.getUnsafe().read("pattern", FancyBedBlock.SheetPattern.CODEC).orElse(FancyBedBlock.SheetPattern.NONE);
    }
}
