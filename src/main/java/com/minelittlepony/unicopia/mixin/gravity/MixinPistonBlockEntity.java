package com.minelittlepony.unicopia.mixin.gravity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonMovingBlockEntity.class)
abstract class MixinPistonBlockEntity {
    @Shadow
    private static AABB moveByPositionAndProgress(BlockPos pos, AABB box, PistonMovingBlockEntity blockEntity) {
        return box;
    }

    @Shadow
    private static boolean matchesStickyCritera(AABB box, Entity entity, BlockPos pos) {
        return false;
    }

    @Shadow
    private static void moveEntityByPiston(Direction direction, Entity entity, double distance, Direction movementDirection) {

    }

    @Inject(method = "moveStuckEntities", at = @At("TAIL"))
    private static void moveEntitiesInHoneyBlock(Level world, BlockPos pos, float ticks, PistonMovingBlockEntity tile, CallbackInfo info) {
        Direction direction = tile.getMovementDirection();
        if (!direction.getAxis().isHorizontal()) {
            return;
        }
        double blockY = tile.getMovedState().getCollisionShape(world, pos).min(Direction.Axis.Y);
        AABB box = moveByPositionAndProgress(pos, new AABB(0, blockY - 1.5000010000000001F, 0, 1, blockY, 1), tile);
        double distance = ticks - tile.getProgress(1);
        for (Entity entity2 : world.getEntities((Entity)null, box, entity -> matchesStickyCritera(box, entity, pos))) {
            moveEntityByPiston(direction, entity2, distance, direction);
        }
    }
}
