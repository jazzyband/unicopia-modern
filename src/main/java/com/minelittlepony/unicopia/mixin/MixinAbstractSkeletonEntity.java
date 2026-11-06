package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.ai.TargettingUtil;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.Vec3;

@Mixin(AbstractSkeleton.class)
abstract class MixinAbstractSkeletonEntity extends Monster {
    MixinAbstractSkeletonEntity() { super(null, null); }

    @Inject(
            method = "performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",
            at = @At(value = "INVOKE",
                target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;",
                shift = Shift.AFTER))
    private void modifyAccuracy(LivingEntity target, float pullProgress, CallbackInfo info, @Local AbstractArrow projectile) {
        if (Equine.of(target).orElse(null) instanceof Pony pony && pony.getPhysics().isFlying()) {
            Vec3 targetPos = TargettingUtil.getProjectedPos(pony.asEntity())
                    .add(0, pony.asEntity().getBbHeight() * 0.33333F, 0)
                    .subtract(pony.asEntity().position());
            projectile.shoot(targetPos.x, targetPos.y + targetPos.horizontalDistance() * 0.2, targetPos.z, 1.6F, (14 - level().getDifficulty().getId() * 4) * 0.25F);
        }
    }
}
