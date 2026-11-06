package com.minelittlepony.unicopia.mixin.gravity;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.Equine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(value = Entity.class, priority = 29000)
abstract class MixinEntity {
    @Shadow
    private BlockPos blockPosition;

    @Inject(method = "getGravity", at = @At("RETURN"), cancellable = true)
    private void modifyGravity(CallbackInfoReturnable<Double> info) {
        if (this instanceof Equine.Container eq) {
            info.setReturnValue(Math.abs(eq.get().getPhysics().calcGravity(info.getReturnValue())));
        }
    }

    @ModifyReturnValue(method = "getGravity()D", at = @At("RETURN"))
    private double modifyGravity(double initial) {
        if (this instanceof Equine.Container eq && !eq.get().asEntity().isNoGravity()) {
            return eq.get().getPhysics().calcGravity(initial);
        }
        return initial;
    }

    @ModifyReturnValue(method = "blockPosition", at = @At("RETURN"))
    private BlockPos invertBlockPos(BlockPos pos) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            return eq.get().getPhysics().getHeadPosition();
        }
        return pos;
    }

    // we invert y when moving
    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 modifyMovement(Vec3 movement) {
        return unicopiaIsGravityInverted() ? movement.multiply(1, -1, 1) : movement;
    }

    // fix on ground check
    @Inject(method = "move", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;verticalCollisionBelow:Z", opcode = Opcodes.PUTFIELD, shift = Shift.AFTER))
    private void onUpdateOnGroundFlag(MoverType movementType, Vec3 movement, CallbackInfo info) {
        if (unicopiaIsGravityInverted()) {
            ((Entity)(Object)this).verticalCollisionBelow = ((Entity)(Object)this).verticalCollision && movement.y > 0.0;
        }
    }

    // invert offsets so it can properly find the block we're walking on
    @ModifyVariable(method = "getOnPos(F)Lnet/minecraft/core/BlockPos;", at = @At("HEAD"), argsOnly = true)
    private float onGetPosWithYOffset(float offset) {
        return unicopiaIsGravityInverted() ? -(((Entity)(Object)this).getBbHeight() + offset) : offset;
    }

    // specifically for players
    @ModifyVariable(
            method = "doCheckFallDamage(DDDZ)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true)
    private double modifyFallDistanceOnFall(double value) {
        return unicopiaIsGravityInverted() ? -value : value;
    }

    // invert check for walking up a step
    @ModifyVariable(
            method = "collideBoundingBox(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            argsOnly = true)
    private static Vec3 modifyMovementForStepheight(Vec3 movement, @Nullable Entity entity) {
        if (entity instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative() && movement.y() == entity.maxUpStep()) {
            return movement.multiply(1, -1, 1);
        }
        return movement;
    }

    @ModifyReturnValue(method = "makeBoundingBox()Lnet/minecraft/world/phys/AABB;", at = @At("RETURN"))
    private AABB adjustPoseBoxForGravity(AABB box) {
        if (unicopiaIsGravityInverted()) {
            Entity self = (Entity)(Object)this;
            AABB oldBox = self.getBoundingBox();
            double newHeight = box.getYsize();
            if (newHeight > oldBox.getYsize()) {
                double targetMaxY = oldBox.maxY;
                Vec3 min = new Vec3(box.minX, targetMaxY - newHeight, box.minZ);
                Vec3 max = new Vec3(box.maxX, targetMaxY, box.maxZ);
                box = new AABB(min, max);
                self.setPosRaw(self.getX(), box.minY, self.getZ());
                return box;
            }
        }

        return box;
    }

    @Unique
    private boolean unicopiaIsGravityInverted() {
        return this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative();
    }
}
