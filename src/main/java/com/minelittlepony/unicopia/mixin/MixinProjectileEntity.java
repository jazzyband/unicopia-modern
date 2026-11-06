package com.minelittlepony.unicopia.mixin;

import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.mob.FlyingVehicleEntity;

@Mixin(Projectile.class)
abstract class MixinProjectileEntity extends Entity {
    private MixinProjectileEntity() { super(null, null); }

    @Inject(method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onOnEntityHit(EntityHitResult hit, CallbackInfo info) {
        Equine.of(hit.getEntity()).ifPresent(eq -> {
            if (eq.onProjectileImpact((Projectile)(Object)this)) {
                info.cancel();
            }
        });
    }
}

@Mixin(ProjectileUtil.class)
abstract class MixinProjectileUtil {
    @ModifyExpressionValue(
            method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getRootVehicle()Lnet/minecraft/world/entity/Entity;", ordinal = 0)
    )
    private static Entity replaceRootVehicle(Entity rootVehicle, Entity entity, Vec3 min, Vec3 max, AABB box, Predicate<Entity> predicate, double maxDistance) {
        return rootVehicle == entity.getRootVehicle() && rootVehicle instanceof FlyingVehicleEntity ? entity : rootVehicle;
    }
}
