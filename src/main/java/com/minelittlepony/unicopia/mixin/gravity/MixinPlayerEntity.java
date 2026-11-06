package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.minelittlepony.unicopia.entity.Equine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Player.class)
abstract class MixinPlayerEntity {
    @ModifyVariable(method = "maybeBackOffFromEdge", at = @At("HEAD"), argsOnly = true)
    private Vec3 flipMovementForSneaking(Vec3 movement) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            return movement.multiply(1, -1, 1);
        }
        return movement;
    }

    @ModifyReturnValue(method = "maybeBackOffFromEdge", at = @At("RETURN"))
    private Vec3 unflipMovementForSneaking(Vec3 result) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            return result.multiply(1, -1, 1);
        }
        return result;
    }

    @ModifyArg(method = "canFallAtLeast",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;<init>(DDDDDD)V"),
            index = 1)
    private double invertStepHeightCheckBoxBottom(double bottom) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            return eq.get().asEntity().getBoundingBox().maxY;
        }
        return bottom;
    }

    @ModifyArg(method = "canFallAtLeast",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;<init>(DDDDDD)V"),
            index = 4)
    private double invertStepHeightCheckBoxTop(double top, @Local(argsOnly = true, ordinal = 2) double stepHeight) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            return eq.get().asEntity().getBoundingBox().maxY + stepHeight + 1.0E-5F;
        }
        return top;
    }

    @ModifyArg(method = "canPlayerFitWithinBlocksAndEntitiesWhen",
            at = @At(value = "INVOKE",
                target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"),
            index = 1)
    private AABB unicopiaReAnchorBoundingBox(AABB box) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            Entity self = eq.get().asEntity();
            AABB oldBox = self.getBoundingBox();
            double newHeight = box.getYsize();
            if (newHeight > oldBox.getYsize()) {
                double targetMaxY = oldBox.maxY;
                Vec3 min = new Vec3(box.minX, targetMaxY - newHeight, box.minZ);
                Vec3 max = new Vec3(box.maxX, targetMaxY, box.maxZ);
                return new AABB(min, max);
            }
        }
        return box;
    }
}
