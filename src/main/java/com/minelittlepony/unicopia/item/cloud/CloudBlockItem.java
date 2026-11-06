package com.minelittlepony.unicopia.item.cloud;

import com.minelittlepony.unicopia.InteractionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CloudBlockItem extends BlockItem {
    public CloudBlockItem(Block block, Item.Properties settings) {
        super(block, settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        InteractionManager.getInstance().sendPlayerLookAngles(user);
        Vec3 targetPos = user.getEyePosition().add(user.getViewVector(1).multiply(1, 1.5, 1).normalize().scale(2));
        BlockPlaceContext context = new BlockPlaceContext(user, hand, user.getItemInHand(hand), new BlockHitResult(
            targetPos,
            Direction.UP,
            BlockPos.containing(targetPos),
            true
        ));

        return place(context);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        return !mustSurvive() || state.canSurvive(context.getLevel(), context.getClickedPos());
    }
}